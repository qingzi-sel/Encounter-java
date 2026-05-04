package com.encounter.service;

import com.encounter.config.GameConfig;
import com.encounter.domain.model.AttributeType;
import com.encounter.domain.model.GameStatus;
import com.encounter.domain.model.ReadingData;
import com.encounter.engine.GameState;
import com.encounter.util.AttributeMath;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages the book reading mini-game with corruption/purify mechanics.
 */
@Service
public class ReadingService {

    private final LogService logService;
    private final GameConfig config;
    private final Random random = new Random();

    private static final String BOOK_20_TEXT =
            "在遥远的虚空中，古老的意志正在复苏。生命的形式并非一成不变，而是充满了变动与诡秘的法则。通过对血液的引导，我们可以窥见进化的真相。这并非诅咒，而是进化的必经之路。然而，意志脆弱者往往会被初期的诱惑所吞噬。保持清醒，观察那些跳动在脉络中的真实色彩。不要相信你眼睛看到的，要相信你内心深处的本能。";

    private static final String BOOK_50_TEXT =
            "当克苏鲁从拉莱耶沉睡中醒来，群星将回归正确的位置。拉莱耶的石柱是由非欧几何构造而成的，即使是最聪明的学者也无法理解其逻辑。那不可名状的恐怖，正在现实的裂缝中悄然生长。当你阅读这些文字时，你的灵魂已经与那个禁忌的世界产生了联系。不要回头看，不要听那些回响在虚空中的耳语。虚空在注视着你，而你也正在成为虚空的一部分。献祭你的理智，换取那片刻的真实。";

    public ReadingService(LogService logService, GameConfig config) {
        this.logService = logService;
        this.config = config;
    }

    /**
     * Start reading a book. Validates prerequisites then initializes ReadingData.
     */
    public void startReading(GameState s, int bookType) {
        if (s.getStatus() != GameStatus.PLAYING) return;

        if (s.getPlayerAttrs().get(AttributeType.INTELLIGENCE) < bookType) {
            logService.addLog(s, "💡 你的智力未达到 " + bookType + "，借阅被拒绝。");
            return;
        }

        String text = bookType == 50 ? BOOK_50_TEXT : BOOK_20_TEXT;
        List<ReadingData.Word> words = new ArrayList<>();
        // Split text into 4-character segments
        for (int i = 0; i < text.length(); i += 4) {
            int end = Math.min(i + 4, text.length());
            String segment = text.substring(i, end);
            words.add(new ReadingData.Word(words.size(), segment, random.nextDouble() * 6 - 3));
        }

        // Seed initial corrupted words
        int initialCorrupt = bookType == 50 ? 2 : 1;
        for (int i = 0; i < initialCorrupt && !words.isEmpty(); i++) {
            words.get(random.nextInt(words.size())).setCorrupt(true);
        }

        s.setReadingData(new ReadingData(bookType, words));
        s.setStatus(GameStatus.READING);
        logService.addLog(s, "📖 你打开了名为 [" + (bookType == 50 ? "拉莱耶残卷" : "活体演化") + "] 的书，文字开始扭曲...");
    }

    /**
     * Tick the reading mini-game. Called from game loop when status == READING.
     */
    public void tick(GameState s, double dt) {
        ReadingData rd = s.getReadingData();
        if (rd == null) {
            s.setStatus(GameStatus.PLAYING);
            return;
        }

        rd.setTimer(rd.getTimer() + dt);
        rd.setSpawnTimer(rd.getSpawnTimer() + dt);

        double spawnInterval = rd.getBookType() == 50
                ? config.getReading().getSpawnInterval50()
                : config.getReading().getSpawnInterval20();

        // Spawn corruptions
        while (rd.getSpawnTimer() >= spawnInterval) {
            rd.setSpawnTimer(rd.getSpawnTimer() - spawnInterval);
            List<ReadingData.Word> normalWords = rd.getWords().stream()
                    .filter(w -> !w.isCorrupt()).toList();
            if (!normalWords.isEmpty()) {
                normalWords.get(random.nextInt(normalWords.size())).setCorrupt(true);
            }
        }

        // Corruption growth based on number of corrupted words
        long corruptCount = rd.getWords().stream().filter(ReadingData.Word::isCorrupt).count();
        double rotRate = rd.getBookType() == 50
                ? config.getReading().getCorruptionRate50()
                : config.getReading().getCorruptionRate20();
        rd.setCorruption(rd.getCorruption() + corruptCount * rotRate * dt);

        // Failure
        if (rd.getCorruption() >= config.getReading().getCorruptionCap()) {
            rd.setCorruption(config.getReading().getCorruptionCap());
            logService.addLog(s, "🚫 [活体篡改失败] 书页彻底陷入疯狂。你的理智受到冲击。");
            s.getPlayerAttrs().set(AttributeType.FOCUS,
                    AttributeMath.snapVal(Math.max(0, s.getPlayerAttrs().get(AttributeType.FOCUS) - config.getReading().getFailureFocusLoss())));
            s.setStatus(GameStatus.PLAYING);
            s.setReadingData(null);
            return;
        }

        // Success
        if (rd.getTimer() >= config.getReading().getDuration()) {
            double reward = rd.getBookType() == 50
                    ? config.getReading().getBook50Reward()
                    : config.getReading().getBook20Reward();
            s.getPlayerAttrs().set(AttributeType.INTELLIGENCE,
                    s.getPlayerAttrs().get(AttributeType.INTELLIGENCE) + reward);
            AttributeMath.snapAllInPlace(s.getPlayerAttrs());
            s.getCompletedBooks().add(rd.getBookType());
            logService.addLog(s, "✅ [活体篡改成功] 你成功驱逐了书页上的疯狂。智力提升了 " + String.format("%.1f", reward) + " 点。");
            s.setStatus(GameStatus.PLAYING);
            s.setReadingData(null);
        }
    }

    /**
     * Purify a corrupted word by clicking it.
     */
    public void purifyWord(GameState s, int wordId) {
        ReadingData rd = s.getReadingData();
        if (rd == null) return;

        rd.getWords().stream()
                .filter(w -> w.getId() == wordId && w.isCorrupt())
                .findFirst()
                .ifPresent(w -> {
                    w.setCorrupt(false);
                    rd.setCorruption(Math.max(0, rd.getCorruption() - config.getReading().getPurifyAmount()));
                });
    }
}
