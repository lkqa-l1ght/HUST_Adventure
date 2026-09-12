package hust.adventure.screens.levels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import hust.adventure.screens.PlayScreen;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LevelNotificationBannerTest {

    @Test
    @DisplayName("Banner starts in inactive/invisible state with zero timer and alpha")
    void bannerInitialState() {
        final LevelNotificationBanner banner = new LevelNotificationBanner();

        assertFalse(banner.isVisible());
        assertEquals(0f, banner.getTimer(), 0.001f);
        assertEquals(0f, banner.getAlpha(), 0.001f);
        assertNull(banner.getMessage());
        assertNull(banner.getTexture());
        assertEquals(LevelNotificationBanner.DEFAULT_DURATION, banner.getDuration(), 0.001f);
    }

    @Test
    @DisplayName("show(message) activates banner with default duration and message")
    void showActivatesBannerWithDefaultDuration() {
        final LevelNotificationBanner banner = new LevelNotificationBanner();
        banner.show("Wave Complete!");

        assertTrue(banner.isVisible());
        assertEquals("Wave Complete!", banner.getMessage());
        assertEquals(LevelNotificationBanner.DEFAULT_DURATION, banner.getTimer(), 0.001f);
        assertEquals(1.0f, banner.getAlpha(), 0.001f);
    }

    @Test
    @DisplayName("show with custom duration and texture sets all attributes correctly")
    void showWithCustomDurationAndTexture() {
        final Texture mockTexture = Mockito.mock(Texture.class);
        final LevelNotificationBanner banner = new LevelNotificationBanner();

        banner.show("Custom Message", mockTexture, 5.0f);

        assertTrue(banner.isVisible());
        assertSame(mockTexture, banner.getTexture());
        assertEquals(5.0f, banner.getDuration(), 0.001f);
        assertEquals(5.0f, banner.getTimer(), 0.001f);
        assertEquals("Custom Message", banner.getMessage());
    }

    @Test
    @DisplayName("update decrements timer and transitions to invisible when expired")
    void updateDecrementsTimer() {
        final LevelNotificationBanner banner = new LevelNotificationBanner();
        banner.show("Objective cleared", 4.0f);

        banner.update(1.5f);
        assertTrue(banner.isVisible());
        assertEquals(2.5f, banner.getTimer(), 0.001f);

        banner.update(2.5f);
        assertFalse(banner.isVisible());
        assertEquals(0.0f, banner.getTimer(), 0.001f);

        // Clamps at zero, does not become negative
        banner.update(1.0f);
        assertEquals(0.0f, banner.getTimer(), 0.001f);
        assertFalse(banner.isVisible());
    }

    @Test
    @DisplayName("Alpha fade calculation maintains full opacity for first half and fades out in second half")
    void alphaFadeCalculation() {
        final LevelNotificationBanner banner = new LevelNotificationBanner();
        banner.show("Alpha Test", 4.0f);

        // At 4.0s (100%): progress = 1.0 -> alpha = min(1.0, 2.0) = 1.0
        assertEquals(1.0f, banner.getAlpha(), 0.001f);

        // At 3.0s (75%): progress = 0.75 -> alpha = min(1.0, 1.5) = 1.0
        banner.update(1.0f);
        assertEquals(1.0f, banner.getAlpha(), 0.001f);

        // At 2.0s (50%): progress = 0.5 -> alpha = min(1.0, 1.0) = 1.0
        banner.update(1.0f);
        assertEquals(1.0f, banner.getAlpha(), 0.001f);

        // At 1.0s (25%): progress = 0.25 -> alpha = 0.5
        banner.update(1.0f);
        assertEquals(0.5f, banner.getAlpha(), 0.001f);

        // At 0.5s (12.5%): progress = 0.125 -> alpha = 0.25
        banner.update(0.5f);
        assertEquals(0.25f, banner.getAlpha(), 0.001f);

        // At 0.0s (0%): progress = 0.0 -> alpha = 0.0
        banner.update(0.5f);
        assertEquals(0.0f, banner.getAlpha(), 0.001f);
    }

    @Test
    @DisplayName("reset forces timer to zero immediately")
    void resetClearsBanner() {
        final LevelNotificationBanner banner = new LevelNotificationBanner();
        banner.show("Active message");
        assertTrue(banner.isVisible());

        banner.reset();
        assertFalse(banner.isVisible());
        assertEquals(0f, banner.getTimer(), 0.001f);
        assertEquals(0f, banner.getAlpha(), 0.001f);
    }

    @Test
    @DisplayName("draw handles null context, batch, font, or inactive state without exception")
    void drawSafeHandlingOfNullsAndInactiveState() {
        final LevelNotificationBanner banner = new LevelNotificationBanner();

        // When inactive
        banner.draw((PlayScreen) null);
        banner.draw((SpriteBatch) null, (BitmapFont) null);

        // When active but parameters are null
        banner.show("Test");
        banner.draw((PlayScreen) null);
        banner.draw((SpriteBatch) null, (BitmapFont) null);

        final SpriteBatch mockBatch = Mockito.mock(SpriteBatch.class);
        banner.draw(mockBatch, null);
        verify(mockBatch, never()).begin();
    }

    @Test
    @DisplayName("draw renders background texture and text when active")
    void drawRendersWhenActive() {
        final Texture mockTexture = Mockito.mock(Texture.class);
        final GlyphLayout mockLayout = Mockito.mock(GlyphLayout.class);
        mockLayout.width = 100f;
        mockLayout.height = 20f;

        final LevelNotificationBanner banner = new LevelNotificationBanner(mockTexture, mockLayout);
        banner.show("Test Banner", 4.0f);

        final SpriteBatch mockBatch = Mockito.mock(SpriteBatch.class);
        final BitmapFont mockFont = Mockito.mock(BitmapFont.class);
        final Color batchColor = new Color(1f, 1f, 1f, 1f);
        final Color fontColor = new Color(0f, 0f, 0f, 1f);

        when(mockBatch.getColor()).thenReturn(batchColor);
        when(mockFont.getColor()).thenReturn(fontColor);
        when(mockBatch.isDrawing()).thenReturn(false);

        banner.draw(mockBatch, mockFont);

        verify(mockBatch).begin();
        verify(mockLayout).setText(mockFont, "Test Banner");
        // boxW = 100 + 40 = 140, boxH = 20 + 30 = 50, boxX = 400 - 70 = 330, boxY = 300 - 25 = 275
        verify(mockBatch).draw(eq(mockTexture), eq(330f), eq(275f), eq(140f), eq(50f));
        // textX = 400 - 50 = 350, textY = 300 + 10 = 310
        verify(mockFont).draw(mockBatch, "Test Banner", 350f, 310f);
        verify(mockBatch).end();
    }

    @Test
    @DisplayName("OutsideBehavior and Floor1Behavior initialize and expose LevelNotificationBanner")
    void behaviorsInitializeAndExposeBanner() {
        final OutsideBehavior outside = new OutsideBehavior();
        final Floor1Behavior floor1 = new Floor1Behavior();

        org.junit.jupiter.api.Assertions.assertNotNull(outside.getBanner());
        org.junit.jupiter.api.Assertions.assertNotNull(floor1.getBanner());
        assertFalse(outside.getBanner().isVisible());
        assertFalse(floor1.getBanner().isVisible());

        final PlayScreen mockContext = Mockito.mock(PlayScreen.class);
        final hust.adventure.HustGame mockGame = Mockito.mock(hust.adventure.HustGame.class);
        final hust.adventure.core.assets.GameAssetManager mockAssetManager = Mockito.mock(hust.adventure.core.assets.GameAssetManager.class);
        final Texture mockTexture = Mockito.mock(Texture.class);
        final hust.adventure.core.data.LevelConfig mockConfig = Mockito.mock(hust.adventure.core.data.LevelConfig.class);
        final hust.adventure.core.data.WaveDataLoader mockWaveData = Mockito.mock(hust.adventure.core.data.WaveDataLoader.class);
        final hust.adventure.entities.factory.EntityFactory mockEntityFactory = Mockito.mock(hust.adventure.entities.factory.EntityFactory.class);

        when(mockContext.getGame()).thenReturn(mockGame);
        when(mockGame.getAssetManager()).thenReturn(mockAssetManager);
        when(mockAssetManager.getTexture(hust.adventure.core.assets.AssetPaths.UI_TEXT_BOX)).thenReturn(mockTexture);
        when(mockContext.getConfig()).thenReturn(mockConfig);
        when(mockConfig.getLevelId()).thenReturn("outside");
        when(mockGame.getWaveDataManager()).thenReturn(mockWaveData);
        when(mockContext.getEntityFactory()).thenReturn(mockEntityFactory);

        outside.init(mockContext);
        assertSame(mockTexture, outside.getBanner().getTexture());

        when(mockConfig.getLevelId()).thenReturn("floor_1");
        floor1.init(mockContext);
        assertSame(mockTexture, floor1.getBanner().getTexture());
    }
}
