package hust.adventure.ui.puzzle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import hust.adventure.core.assets.AssetPaths;
import hust.adventure.events.EventDispatcher;
import hust.adventure.screens.PlayScreen;

/**
 * Memory Card matching mini-game puzzle.
 */
public class MemoryCardPuzzle extends BasePuzzleGame {
    private static final int NUM_CARDS = 36;
    private static final int NUM_PAIRS = 18;
    private static final float TIME_LIMIT = 180f; // 3 minutes

    public enum MatchState {
        IDLE, FIRST_CARD_SELECTED, SECOND_CARD_SELECTED, MISMATCH_DELAY
    }

    private MatchState state;

    private float timeRemaining;
    private final MemoryCard[] cards = new MemoryCard[NUM_CARDS];
    private final int[] cardIds = new int[NUM_CARDS];

    private int firstSelectedIndex;
    private int secondSelectedIndex;
    private float mismatchTimer;

    // Assets eager binding
    private Texture backTexture;
    private final Texture[] faceTextures = new Texture[NUM_PAIRS];
    private final StringBuilder textBuilder = new StringBuilder();
    private final StringBuilder tempBuilder = new StringBuilder();

    @Override
    public void init(final PlayScreen ctx) {
        super.init(ctx);

        // Card dimensions & positioning parameters
        final float cardWidth = 49f;
        final float cardHeight = 65f;
        final float gapX = 15f;
        final float gapY = 10f;
        final float startX = 215.5f;
        final float startY = 85f;

        // Create cards grid
        for (int i = 0; i < NUM_CARDS; i++) {
            final int col = i % 6;
            final int row = i / 6;
            final float x = startX + col * (cardWidth + gapX);
            final float y = startY + row * (cardHeight + gapY);
            cards[i] = new MemoryCard(0, x, y, cardWidth, cardHeight);
        }

        // Cache textures from GameAssetManager
        backTexture = ctx.getGame().getAssetManager().getTexture("puzzle/cards/card_back.png");
        for (int i = 0; i < NUM_PAIRS; i++) {
            // Reusable buffer to avoid allocations
            tempBuilder.setLength(0);
            tempBuilder.append("puzzle/cards/card_face_");
            if (i + 1 < 10) {
                tempBuilder.append("0");
            }
            tempBuilder.append(i + 1).append(".png");
            faceTextures[i] = ctx.getGame().getAssetManager().getTexture(tempBuilder.toString());
        }

        reset();
    }

    @Override
    public void reset() {
        this.isSolved = false;
        this.showIntro = true;
        this.timeRemaining = TIME_LIMIT;
        this.firstSelectedIndex = -1;
        this.secondSelectedIndex = -1;
        this.state = MatchState.IDLE;

        // Generate paired layout: two of each ID from 1 to 18
        for (int i = 0; i < NUM_PAIRS; i++) {
            cardIds[i * 2] = i + 1;
            cardIds[i * 2 + 1] = i + 1;
        }

        // Fisher-Yates Shuffle
        for (int i = NUM_CARDS - 1; i > 0; i--) {
            final int j = MathUtils.random(i);
            final int temp = cardIds[i];
            cardIds[i] = cardIds[j];
            cardIds[j] = temp;
        }

        // Configure pre-allocated memory cards
        for (int i = 0; i < NUM_CARDS; i++) {
            cards[i].setPairId(cardIds[i]);
            cards[i].setFlipped(false);
            cards[i].setMatched(false);
        }
    }

    @Override
    public void update(final float delta) {
        if (isSolved) {
            return;
        }

        if (checkIntroClick()) {
            return;
        }
        if (showIntro) {
            return;
        }

        timeRemaining -= delta;
        if (timeRemaining <= 0f) {
            EventDispatcher.getInstance().playSfx(AssetPaths.SFX_PUZZLE_FAILED);
            reset();
            return;
        }

        if (state == MatchState.MISMATCH_DELAY) {
            mismatchTimer -= delta;
            if (mismatchTimer <= 0) {
                if (firstSelectedIndex != -1 && secondSelectedIndex != -1) {
                    cards[firstSelectedIndex].setFlipped(false);
                    cards[secondSelectedIndex].setFlipped(false);
                }
                firstSelectedIndex = -1;
                secondSelectedIndex = -1;
                state = MatchState.IDLE;
            }
            return;
        }

        handleInput();
    }

    private void handleInput() {
        if (Gdx.input.justTouched() && context != null) {
            tmpMouse.set(Gdx.input.getX(), Gdx.input.getY());
            context.unproject(tmpMouse);
            final float mx = tmpMouse.x;
            final float my = tmpMouse.y;

            for (int i = 0; i < NUM_CARDS; i++) {
                final MemoryCard card = cards[i];
                if (card.getBounds().contains(mx, my)) {
                    if (card.isMatched() || card.isFlipped()) {
                        return; // Ignore if already matched or flipped
                    }

                    if (state == MatchState.IDLE) {
                        card.setFlipped(true);
                        playFlipSound();
                        firstSelectedIndex = i;
                        state = MatchState.FIRST_CARD_SELECTED;
                    } else if (state == MatchState.FIRST_CARD_SELECTED) {
                        if (i == firstSelectedIndex) {
                            return; // Ignore clicking same card
                        }
                        card.setFlipped(true);
                        playFlipSound();
                        secondSelectedIndex = i;
                        state = MatchState.SECOND_CARD_SELECTED;
                        evaluateMatch();
                    }
                    break;
                }
            }
        }
    }

    private void evaluateMatch() {
        final MemoryCard card1 = cards[firstSelectedIndex];
        final MemoryCard card2 = cards[secondSelectedIndex];

        if (card1.getPairId() == card2.getPairId()) {
            card1.setMatched(true);
            card2.setMatched(true);
            EventDispatcher.getInstance().playSfx(AssetPaths.SFX_ANSWER_CORRECT);
            firstSelectedIndex = -1;
            secondSelectedIndex = -1;
            state = MatchState.IDLE;

            checkWinCondition();
        } else {
            EventDispatcher.getInstance().playSfx(AssetPaths.SFX_ANSWER_WRONG);
            state = MatchState.MISMATCH_DELAY;
            mismatchTimer = 1.0f;
        }
    }

    private void checkWinCondition() {
        boolean allMatched = true;
        for (int i = 0; i < NUM_CARDS; i++) {
            if (!cards[i].isMatched()) {
                allMatched = false;
                break;
            }
        }
        if (allMatched) {
            isSolved = true;
        }
    }

    private void playFlipSound() {
        EventDispatcher.getInstance().playSfx(AssetPaths.SFX_CARD_FLIP);
    }


    @Override
    public void render(final ShapeRenderer shapeRenderer, final SpriteBatch batch) {
        if (isSolved) {
            return;
        }

        if (showIntro) {
            drawIntro(shapeRenderer, batch);
            return;
        }

        // Draw cards
        batch.begin();
        for (int i = 0; i < NUM_CARDS; i++) {
            final MemoryCard card = cards[i];
            final Texture tex = (card.isFlipped() || card.isMatched()) ? faceTextures[card.getPairId() - 1]
                    : backTexture;
            batch.draw(tex, card.getBounds().x, card.getBounds().y, card.getBounds().width, card.getBounds().height);
        }

        // Render timer overlay numbers
        final BitmapFont font = context.getFont();
        font.setColor(Color.WHITE);

        textBuilder.setLength(0);
        textBuilder.append("Ghép thẻ - Thời gian: ").append((int) timeRemaining).append("s");

        if (textBoxTexture != null) {
            drawTextInBox(batch, font, textBuilder, 400f, 555f, textBoxTexture, textLayout);
        } else {
            font.draw(batch, textBuilder, 20f, 580f);
        }

        batch.end();

        // Highlight selected cards & draw timer bar
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.YELLOW);
        if (firstSelectedIndex != -1) {
            final MemoryCard card = cards[firstSelectedIndex];
            shapeRenderer.rect(card.getBounds().x, card.getBounds().y, card.getBounds().width, card.getBounds().height);
        }
        if (secondSelectedIndex != -1) {
            final MemoryCard card = cards[secondSelectedIndex];
            shapeRenderer.rect(card.getBounds().x, card.getBounds().y, card.getBounds().width, card.getBounds().height);
        }
        shapeRenderer.end();

        // Render top shrinking timer bar
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (timeRemaining > 90f) {
            shapeRenderer.setColor(Color.GREEN);
        } else if (timeRemaining > 30f) {
            shapeRenderer.setColor(Color.YELLOW);
        } else {
            shapeRenderer.setColor(Color.RED);
        }
        final float timerWidth = 800f * (timeRemaining / TIME_LIMIT);
        shapeRenderer.rect(0f, 590f, timerWidth, 10f);
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        // No custom fonts/textures instantiated locally, using shared context components
    }

    private void drawIntro(final ShapeRenderer shapeRenderer, final SpriteBatch batch) {
        final String introText = "Chào mừng bạn đến với thử thách thứ hai!\n\n" + "Luật chơi Lật Bài rất đơn giản:\n"
                + "1. Trên màn hình là 36 tấm thẻ chứa các khái niệm lập trình.\n"
                + "2. Hãy click để lật các thẻ lên và tìm các cặp thẻ giống nhau.\n"
                + "3. Bạn được phép sai nhiều lần, nhưng phải ghép đúng toàn bộ.\n"
                + "4. Hoàn thành toàn bộ cặp bài trước khi hết 3 phút.\n\n"
                + "Hãy nhấn nút bên dưới để bắt đầu lật bài!";

        drawIntroModal(shapeRenderer, batch, "THỬ THÁCH 2: LẬT BÀI CẶP", introText);
    }
}
