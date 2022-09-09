package thecsdev.betterstats.util.math;

public class Tuple<K, V>
{
	public final K Item1;
	public final V Item2;
	
	public Tuple(K item1, V item2)
	{
		this.Item1 = item1;
		this.Item2 = item2;
	}
}