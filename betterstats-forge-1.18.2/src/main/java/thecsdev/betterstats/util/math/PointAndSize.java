package thecsdev.betterstats.util.math;

import java.awt.Dimension;
import java.awt.Point;

public class PointAndSize
{
	public static final PointAndSize ZERO = new PointAndSize();
	
	public int x, y, width, height;
	
	public PointAndSize() { this(0,0,0,0); }
	public PointAndSize(int x, int y) { this(x, y, 0, 0); }
	public PointAndSize(int x, int y, int width, int height)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public Point getPosition() { return new Point(x, y); }
	public Dimension getDimension() { return new Dimension(width, height); }
	
	public boolean isHovering(double mouseX, double mouseY)
	{
		return (mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height);
	}
}