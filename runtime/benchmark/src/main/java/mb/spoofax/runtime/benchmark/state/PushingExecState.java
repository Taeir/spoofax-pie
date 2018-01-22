package mb.spoofax.runtime.benchmark.state;

import kotlin.Unit;
import mb.pie.runtime.core.ExecException;
import mb.pie.runtime.core.PushingExecutor;
import mb.pie.runtime.core.impl.PushingExecutorImpl;
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
public class PushingExecState {
    public PushingExecutor executor;

    public void setup(SpoofaxPieState spoofaxPieState, WorkspaceState workspaceState, InfraState infraState) {
        init(spoofaxPieState, workspaceState, infraState);
    }

    public void renew(SpoofaxPieState spoofaxPieState, WorkspaceState workspaceState, InfraState infraState) {
        init(spoofaxPieState, workspaceState, infraState);
    }

    public void exec(WorkspaceState workspaceState, List<PPath> changedPaths) {
        executor.pathsChanged(changedPaths);
        executor.dirtyFlag();
        try {
            executor.executeAll(new NullCancelled());
        } catch(ExecException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void init(SpoofaxPieState spoofaxPieState, WorkspaceState workspaceState, InfraState infraState) {
        this.executor =
            new PushingExecutorImpl(infraState.store, infraState.cache, infraState.share, infraState.layer,
                infraState.logger, infraState.funcs, spoofaxPieState.logger);
        final PPath root = workspaceState.root;
        try(final Stream<PPath> stream = root.list(PPaths.directoryPathMatcher())) {
            stream
                .filter((path) -> !path.toString().contains("root"))
                .forEach((path) -> executor.add(path,
                    SpoofaxPipeline.INSTANCE.processProjectObsFunApp(path, root, o -> Unit.INSTANCE)));
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}