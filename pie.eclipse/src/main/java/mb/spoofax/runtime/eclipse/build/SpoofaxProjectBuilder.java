package mb.spoofax.runtime.eclipse.build;

import com.google.inject.Injector;
import java.util.Map;
import mb.log.api.Logger;
import mb.pie.api.ExecException;
import mb.spoofax.runtime.eclipse.SpoofaxPlugin;
import mb.spoofax.runtime.eclipse.pipeline.PipelineAdapter;
import mb.spoofax.runtime.eclipse.util.Nullable;
import mb.spoofax.runtime.eclipse.util.StatusUtils;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

public class SpoofaxProjectBuilder extends IncrementalProjectBuilder {
    public static final String id = SpoofaxPlugin.id + ".builder";

    private final Logger logger;
    private final PipelineAdapter pipelineAdapter;


    public SpoofaxProjectBuilder() {
        final Injector injector = SpoofaxPlugin.spoofaxFacade().injector;
        this.logger = injector.getInstance(Logger.class).forContext(getClass());
        this.pipelineAdapter = injector.getInstance(PipelineAdapter.class);
    }


    @Override protected @Nullable IProject[] build(int kind, @Nullable Map<String, String> args,
        @Nullable IProgressMonitor monitor) throws CoreException {
        final IProject project = getProject();
        final IResourceDelta delta = getDelta(project);
        try {
            pipelineAdapter.buildProject(project, kind, delta, monitor);
        } catch(@SuppressWarnings("unused") InterruptedException e) {
            logger.debug("Project build cancelled");
            rememberLastBuiltState();
        } catch(ExecException e) {
            final String msg = "Project build failed unexpectedly";
            logger.error(msg, e);
            throw new CoreException(StatusUtils.buildFailure(msg, e));
        }

        return null;
    }

    @Override protected void clean(@Nullable IProgressMonitor monitor) throws CoreException {
        pipelineAdapter.cleanAll(monitor);
        forgetLastBuiltState();
    }

    @Override public ISchedulingRule getRule(int kind, Map<String, String> args) {
        return null;
    }
}
