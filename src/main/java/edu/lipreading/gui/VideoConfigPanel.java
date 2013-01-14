package edu.lipreading.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Vector;

import static com.googlecode.javacv.cpp.opencv_core.cvCircle;

import com.googlecode.javacv.cpp.opencv_core.CvArr;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.lipreading.Constants;
import edu.lipreading.vision.StickerColorConfiguration;
import javax.swing.JLabel;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

public class VideoConfigPanel extends VideoCapturePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Vector<Sticker> stickers;

	/**
	 * Create the panel.
	 */
	public VideoConfigPanel() {
		super();
		canvas.setBackground(UIManager.getColor("InternalFrame.inactiveTitleGradient"));
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				for (Sticker sticker : stickers){				 
					if (sticker.isClicked(arg0.getPoint())){
						//TODO Add semaphore that only one sticker can be clicked each time:
						sticker.setColor(Color.gray); // TODO - Color in better colors
						repaint();
						canvas.addStickerEventListener(sticker);
						break;


					}

				}


			}
		});
		setBackground(Color.WHITE);
		initStickers();
		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				Point mousePoint = arg0.getPoint();
				BufferedImage scaledImg = UIUtils.resizeImage(image, canvas.getWidth(), canvas.getHeight());
				int stickerRGB = scaledImg.getRGB((int)mousePoint.getX(), (int)mousePoint.getY());

				Color c = new Color(stickerRGB);

				if (canvas.stickerListener !=null){
					canvas.stickerListener.ClickedOnColor(c);
				}

			}
		});
		setLayout(null);

		canvas.setBounds(331, 10, 375, 303);

		JLabel lblNewLabel = new JLabel("Click on left sticker and then click on its real position in the right video");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setForeground(Color.GRAY);
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblNewLabel.setBounds(10, 389, 696, 59);
		add(lblNewLabel);

		JLabel lipsPic = new JLabel("");
		lipsPic.setIcon(new ImageIcon(getClass().getResource(Constants.LIP_IMAGE_FILE_PATH)));
		lipsPic.setBounds(64, 107, 168, 104);
		add(lipsPic);

	}



	public void paint(Graphics g)  
	{  
		super.paint(g);
		for (Sticker sticker : stickers){				 
			g.drawOval(sticker.x, sticker.y, sticker.radius, sticker.radius);
			g.setColor(sticker.color);
			g.fillOval(sticker.x, sticker.y, sticker.radius, sticker.radius);
		}
	}

	private void initStickers()
	{
		stickers = new Vector<Sticker>();
		stickers.add(new Sticker(136, 102, 20, Color.red, StickerLocation.UPPER));
		stickers.add(new Sticker(64, 147, 20, Color.green, StickerLocation.LEFT));
		stickers.add(new Sticker(220, 147, 20, Color.blue, StickerLocation.RIGHT));
		stickers.add(new Sticker(136, 203, 20, Color.yellow, StickerLocation.LOWER));	   

	}



	public class Sticker implements StickerEvent
	{
		private int x, y, radius;
		private Color color;
		private boolean isPressed;
		private StickerLocation stickerLocation;

		public Sticker(int x, int y, int radius, Color c, StickerLocation stickerLocation){
			this.x = x;
			this.y = y;
			this.radius = radius;
			this.color = c;
			this.stickerLocation = stickerLocation;
		}

		public boolean isClicked(Point point) {
			return (Math.pow((point.x-x), 2) + Math.pow((point.y - y),2)) < Math.pow(radius, 2);
		}

		public boolean getIsPressed() {
			return isPressed;
		}

		public void setIsPressed(boolean b) {
			isPressed = b;
		}

		public void setColor(Color c){
			this.color = c;
		}

		@Override
		public void ClickedOnColor(Color c) {

			// Update general stickers colors:
			switch (this.stickerLocation) {
			case UPPER:
				StickerColorConfiguration.UPPER_STICKER_MAX = new CvScalar(c.getBlue() + 20, c.getGreen() + 20, c.getRed() + 20,0);
				StickerColorConfiguration.UPPER_STICKER_MIN = new CvScalar(c.getBlue() - 20, c.getGreen() - 20, c.getRed() - 20,0);
				break;
			case LOWER:
				StickerColorConfiguration.LOWER_STICKER_MAX = new CvScalar(c.getBlue() + 20, c.getGreen() + 20, c.getRed() + 20,0);
				StickerColorConfiguration.LOWER_STICKER_MIN = new CvScalar(c.getBlue() - 20, c.getGreen() - 20, c.getRed() - 20,0);
				break;
			case LEFT:
				StickerColorConfiguration.LEFT_STICKER_MAX = new CvScalar(c.getBlue() + 20, c.getGreen() + 20, c.getRed() + 20,0);
				StickerColorConfiguration.LEFT_STICKER_MIN = new CvScalar(c.getBlue() - 20, c.getGreen() - 20, c.getRed() - 20,0);
				break;
			case RIGHT:
				StickerColorConfiguration.RIGHT_STICKER_MAX = new CvScalar(c.getBlue() + 20, c.getGreen() + 20, c.getRed() + 20,0);
				StickerColorConfiguration.RIGHT_STICKER_MIN = new CvScalar(c.getBlue() - 20, c.getGreen() - 20, c.getRed() - 20,0);
				break;
			default:
				break;
			}

			setColor(c);
			repaint();
			canvas.addStickerEventListener(null);
		}

	}

	// Listener interface
	public interface StickerEvent {
		void ClickedOnColor(Color c);
	}

	@Override
	protected void getVideoFromSource() throws com.googlecode.javacv.FrameGrabber.Exception {
		try {

			IplImage grabbed;
			while(!threadStop.get()){
				synchronized (threadStop) {
					if((grabbed = grabber.grab()) == null)
						break;
				}
				List<Integer> frameCoordinates = stickersExtractor.getPoints(grabbed);
				for (int i=0; i< Constants.POINT_COUNT; i++){
					CvScalar color = null;
					switch (i){
					case Constants.UPPER_VECTOR_INDEX:
						color = StickerColorConfiguration.UPPER_STICKER_MAX;
						break;
					case Constants.LOWER_VECTOR_INDEX:
						color = StickerColorConfiguration.LOWER_STICKER_MAX;
						break;
					case Constants.LEFT_VECTOR_INDEX:
						color = StickerColorConfiguration.LEFT_STICKER_MAX;
						break;
					case Constants.RIGHT_VECTOR_INDEX:
						color = StickerColorConfiguration.RIGHT_STICKER_MAX;
						break;
					}
					int x = frameCoordinates.get(i * 2);
					int y = frameCoordinates.get((i * 2) + 1);
					if (x != 0 && y!=0)
						cvCircle((CvArr)grabbed, new CvPoint(x, y), 20, color, 3, 0, 0);
					image = grabbed.getBufferedImage();
					canvas.setImage(image);
					canvas.paint(null);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
