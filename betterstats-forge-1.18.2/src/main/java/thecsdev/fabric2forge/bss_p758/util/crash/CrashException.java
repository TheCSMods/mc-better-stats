package thecsdev.fabric2forge.bss_p758.util.crash;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;

public class CrashException extends ReportedException
{
	private static final long serialVersionUID = -1046046668419459329L;
	public CrashException(CrashReport crashReport) { super(crashReport); }
}