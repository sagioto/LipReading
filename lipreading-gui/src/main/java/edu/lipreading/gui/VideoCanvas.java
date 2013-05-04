package edu.lipreading.gui;

import edu.lipreading.gui.StickersConfigPanel.StickerEvent;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

public class VideoCanvas extends Canvas{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected BufferedImage image = null;
	
	// Our collection of classes that are subscribed as listeners of our
    protected StickerEvent stickerListener;
	


	public VideoCanvas() {
		// TODO Auto-generated constructor stub
	}
	
    @Override public void update(Graphics g) { 
        paint(g);
    }
    @Override public void paint(Graphics g) {
        // Calling BufferStrategy.show() here sometimes throws
        // NullPointerException or IllegalStateException,
        // but otherwise seems to work fine.
        try {
            BufferStrategy strategy = this.getBufferStrategy();
            do {
                do {
                    g = strategy.getDrawGraphics();
                    if (image != null) {
                        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
                    }
                    g.dispose();
                } while (strategy.contentsRestored());
                strategy.show();
            } while (strategy.contentsLost());
        } catch (NullPointerException ignored) {
        } catch (IllegalStateException ignored) { }
    }

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

    public void addStickerEventListener(StickerEvent listener)
    {
    	stickerListener = listener;
    }

}
