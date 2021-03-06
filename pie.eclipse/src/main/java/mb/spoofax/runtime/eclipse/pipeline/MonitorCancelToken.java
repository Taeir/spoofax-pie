package mb.spoofax.runtime.eclipse.pipeline;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import mb.pie.api.exec.Cancel;
import mb.pie.api.exec.Cancelled;

public class MonitorCancelToken implements Cancelled, Cancel {
    public SubMonitor monitor;

    public MonitorCancelToken(IProgressMonitor monitor) {
        this.monitor = SubMonitor.convert(monitor);
    }

    public MonitorCancelToken(SubMonitor monitor) {
        this.monitor = monitor;
    }

    @Override public void requestCancel() {
        monitor.setCanceled(true);
    }

    @Override public boolean isCancelled() {
        return monitor.isCanceled();
    }

    @Override public void throwIfCancelled() throws InterruptedException {
        if(monitor.isCanceled()) {
            throw new InterruptedException();
        }
    }
}
