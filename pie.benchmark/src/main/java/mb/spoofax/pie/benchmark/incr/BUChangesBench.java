package mb.spoofax.pie.benchmark.incr;

import mb.spoofax.pie.benchmark.Timer;
import mb.spoofax.pie.benchmark.state.exec.BUState;
import mb.spoofax.pie.benchmark.state.ChangesState;
import mb.spoofax.pie.benchmark.state.InfraState;
import mb.spoofax.pie.benchmark.state.SpoofaxPieState;
import mb.spoofax.pie.benchmark.state.WorkspaceState;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class BUChangesBench {
    @Setup(Level.Trial)
    public void setupTrial(SpoofaxPieState spoofaxPie, WorkspaceState workspace, InfraState infra, ChangesState changes, BUState exec) {
        workspace.setup(spoofaxPie);
        infra.setup(spoofaxPie, workspace);
        changes.setup(workspace);
        exec.setup(spoofaxPie, workspace, infra);

        this.spoofaxPie = spoofaxPie;
        this.workspace = workspace;
        this.infra = infra;
        this.changes = changes;
        this.exec = exec;
    }

    private SpoofaxPieState spoofaxPie;
    private WorkspaceState workspace;
    private InfraState infra;
    private ChangesState changes;
    private BUState exec;

    @Setup(Level.Invocation) public void setupInvocation() {
        Timer.logFile = new File("/Users/gohla/pie/bottomup.csv");
        Timer.clearFile();
        infra.reset();
        changes.reset();
        exec.setup(spoofaxPie, workspace, infra);
        exec.reset();
    }

    @Benchmark public void exec(Blackhole blackhole) {
        changes.exec(exec, blackhole);
    }

    @TearDown(Level.Trial) public void tearDownTrial() {
        infra.reset();
        changes.reset();
        exec.reset();
    }
}
