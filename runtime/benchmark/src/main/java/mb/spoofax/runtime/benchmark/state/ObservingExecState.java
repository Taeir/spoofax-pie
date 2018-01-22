package mb.spoofax.runtime.benchmark.state;

import kotlin.Unit;
import mb.pie.runtime.core.ExecException;
import mb.pie.runtime.core.ObservingExecutor;
import mb.pie.runtime.core.impl.ObservingExecutorImpl;
import mb.spoofax.runtime.pie.builder.SpoofaxPipeline;
import mb.util.async.NullCancelled;
import mb.vfs.path.PPath;
import mb.vfs.path.PPaths;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;


@State(Scope.Benchmark)
public class ObservingExecState {
    public ObservingExecutor executor;

    public void setup(SpoofaxPieState spoofaxPieState, WorkspaceState workspaceState, InfraState infraState) {
        init(spoofaxPieState, workspaceState, infraState);
    }

    public void renew(SpoofaxPieState spoofaxPieState, WorkspaceState workspaceState, InfraState infraState) {
        init(spoofaxPieState, workspaceState, infraState);
    }

    public void exec(WorkspaceState workspaceState, List<PPath> changedPaths) {
        try {
            executor.pathsChanged(changedPaths, new NullCancelled());
        } catch(ExecException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void init(SpoofaxPieState spoofaxPieState, WorkspaceState workspaceState, InfraState infraState) {
        this.executor =
            new ObservingExecutorImpl(infraState.store, infraState.cache, infraState.share, infraState.layer,
                infraState.logger, spoofaxPieState.logger, infraState.funcs);
        final PPath root = workspaceState.root;
        try(final Stream<PPath> stream = root.list(PPaths.directoryPathMatcher())) {
            for(PPath path : (Iterable<PPath>) stream.filter((path) -> !path.toString().contains("root"))::iterator) {
                executor.setObserver(SpoofaxPipeline.INSTANCE.processProjectFunApp(path, root),
                    (result) -> Unit.INSTANCE, new NullCancelled());
            }
        } catch(ExecException | InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}