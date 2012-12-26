package edu.lipreading.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

import static com.googlecode.javacv.cpp.opencv_core.cvCircle;
import static com.googlecode.javacv.cpp.opencv_core.cvScalar;

import com.googlecode.javacv.cpp.opencv_core.CvArr;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import edu.lipreading.vision.VideoConfiguration;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingConstants;

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
				int stickerRGB = image.getRGB((int)mousePoint.getX(), (int)mousePoint.getY());
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
		    stickers.add(new Sticker(136, 64, 20, Color.red, StickerLocation.UPPER));
		    stickers.add(new Sticker(80, 116, 20, Color.green, StickerLocation.LEFT));
		    stickers.add(new Sticker(192, 116, 20, Color.blue, StickerLocation.RIGHT));
		    stickers.add(new Sticker(136, 168, 20, Color.yellow, StickerLocation.LOWER));	   
		    
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
					VideoConfiguration.UPPER_STICKER_MAX = new CvScalar(c.getBlue() + 20, c.getGreen() + 20, c.getRed() + 20,0);
					VideoConfiguration.UPPER_STICKER_MIN = new CvScalar(c.getBlue() - 20, c.getGreen() - 20, c.getRed() - 20,0);
					break;
				case LOWER:
					VideoConfiguration.LOWER_STICKER_MAX = new CvScalar(c.getBlue() + 20, c.getGreen() + 20, c.getRed() + 20,0);
					VideoConfiguration.LOWER_STICKER_MIN = new CvScalar(c.getBlue() - 20, c.getGreen() - 20, c.getRed() - 20,0);
					break;
				case LEFT:
					VideoConfiguration.LEFT_STICKER_MAX = new CvScalar(c.getBlue() + 20, c.getGreen() + 20, c.getRed() + 20,0);
					VideoConfiguration.LEFT_STICKER_MIN = new CvScalar(c.getBlue() - 20, c.getGreen() - 20, c.getRed() - 20,0);
					break;
				case RIGHT:
					VideoConfiguration.RIGHT_STICKER_MAX = new CvScalar(c.getBlue() + 20, c.getGreen() + 20, c.getRed() + 20,0);
					VideoConfiguration.RIGHT_STICKER_MIN = new CvScalar(c.getBlue() - 20, c.getGreen() - 20, c.getRed() - 20,0);
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
		  protected void getVideoFromCamera() throws com.googlecode.javacv.FrameGrabber.Exception {
				try {
					
					IplImage grabbed;
					while(!threadStop){
						synchronized (threadStop) {
							if (!threadStop)
							{
								if ((grabbed = grabber.grab()) == null)
									break;
								List<Integer> frameCoordinates = stickersExt.getPoints(grabbed);
								for (int i=0; i< VideoConfiguration.NUM_OF_STICKERS; i++){
									CvScalar color = null;
									switch (i){
									case VideoConfiguration.UPPER_VECTOR_INDEX:
										color = cvScalar(0, 0, 255, 0);
										//color = VideoConfiguration.UPPER_STICKER_MAX;//TODO - Make an average of max & min
										break;
									case VideoConfiguration.LOWER_VECTOR_INDEX:
										color = VideoConfiguration.LOWER_STICKER_MAX;
										break;
									case VideoConfiguration.LEFT_VECTOR_INDEX:
										color = VideoConfiguration.LEFT_STICKER_MAX;
										break;
									case VideoConfiguration.RIGHT_VECTOR_INDEX:
										color = VideoConfiguration.RIGHT_STICKER_MAX;
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
						}
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
}
