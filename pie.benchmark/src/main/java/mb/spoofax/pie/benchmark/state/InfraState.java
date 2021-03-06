package mb.spoofax.pie.benchmark.state;

import mb.log.api.Logger;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.pie.api.stamp.*;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.layer.NoopLayer;
import mb.pie.runtime.layer.ValidationLayer;
import mb.pie.runtime.logger.NoopLogger;
import mb.pie.runtime.logger.StreamLogger;
import mb.pie.runtime.logger.exec.LoggerExecutorLogger;
import mb.pie.runtime.logger.exec.NoopExecutorLogger;
import mb.pie.runtime.share.NonSharingShare;
import mb.pie.runtime.store.InMemoryStore;
import mb.pie.share.coroutine.CoroutineShareKt;
import mb.pie.store.lmdb.LMDBStoreKt;
import mb.pie.taskdefs.guice.GuiceTaskDefsKt;
import mb.spoofax.pie.LogLoggerKt;
import org.openjdk.jmh.annotations.*;

import java.io.File;


@State(Scope.Benchmark)
public class InfraState {
    public Pie pie;

    public void setup(SpoofaxPieState spoofaxPieState, WorkspaceState workspaceState) {
        final PieBuilder builder = new PieBuilderImpl();
        storeKind.apply(builder, spoofaxPieState, workspaceState);
        shareKind.apply(builder);
        builder.withDefaultOutputStamper(defaultOutputStamperKind.get());
        builder.withDefaultFileReqStamper(defaultFileReqStamperKind.get());
        builder.withDefaultFileGenStamper(defaultFileGenStamperKind.get());
        layerKind.apply(builder);
        loggerKind.apply(builder, spoofaxPieState);
        executorLoggerKind.apply(builder);
        GuiceTaskDefsKt.withGuiceTaskDefs(builder, spoofaxPieState.injector);
        final Pie pie = builder.build();
        pie.dropStore();
        this.pie = pie;
    }

    public void reset() {
        pie.dropStore();
    }


    @Param({"in_memory"}) private StoreKind storeKind;
    @Param({"non_sharing"}) private ShareKind shareKind;
    @Param({"equals"}) private OutputStamperKind defaultOutputStamperKind;
    @Param({"modified"}) private FileStamperKind defaultFileReqStamperKind;
    @Param({"modified"}) private FileStamperKind defaultFileGenStamperKind;
    @Param({"validation"}) private LayerKind layerKind;
    @Param({"noop"}) private LoggerKind loggerKind;
    @Param({"noop"}) private ExecutorLoggerKind executorLoggerKind;

    @SuppressWarnings("unused") public enum StoreKind {
        lmdb {
            @Override public void apply(PieBuilder builder, SpoofaxPieState spoofaxPieState, WorkspaceState workspaceState) {
                final File localStorePath = spoofaxPieState.pathSrv.localPath(workspaceState.storePath);
                if(localStorePath == null) {
                    throw new RuntimeException(
                        "Cannot create LMDB store, cannot convert " + workspaceState.storePath + " to a local filesystem path");
                }
                LMDBStoreKt.withLMDBStore(builder, localStorePath);
            }
        },
        in_memory {
            @Override public void apply(PieBuilder builder, SpoofaxPieState spoofaxPieState, WorkspaceState workspaceState) {
                builder.withStore(logger -> new InMemoryStore());
            }
        };

        public abstract void apply(PieBuilder builder, SpoofaxPieState spoofaxPieState, WorkspaceState workspaceState);
    }

    @SuppressWarnings("unused") public enum ShareKind {
        coroutine {
            @Override public void apply(PieBuilder builder) {
                CoroutineShareKt.withCoroutineShare(builder);
            }
        },
        non_sharing {
            @Override public void apply(PieBuilder builder) {
                builder.withShare(logger -> new NonSharingShare());
            }
        };

        public abstract void apply(PieBuilder builder);
    }

    @SuppressWarnings("unused") public enum OutputStamperKind {
        equals {
            @Override public OutputStamper get() {
                return OutputStampers.INSTANCE.getEquals();
            }
        },
        inconsequential {
            @Override public OutputStamper get() {
                return OutputStampers.INSTANCE.getInconsequential();
            }
        };

        public abstract OutputStamper get();
    }

    @SuppressWarnings("unused") public enum FileStamperKind {
        exists {
            @Override public FileStamper get() {
                return FileStampers.INSTANCE.getExists();
            }
        },
        modified {
            @Override public FileStamper get() {
                return FileStampers.INSTANCE.getModified();
            }
        },
        hash {
            @Override public FileStamper get() {
                return FileStampers.INSTANCE.getHash();
            }
        };

        public abstract FileStamper get();
    }

    @SuppressWarnings("unused") public enum LayerKind {
        validation {
            @Override public void apply(PieBuilder builder) {
                builder.withLayer(ValidationLayer::new);
            }
        },
        noop {
            @Override public void apply(PieBuilder builder) {
                builder.withLayer(logger -> new NoopLayer());
            }
        };

        public abstract void apply(PieBuilder builder);
    }

    @SuppressWarnings("unused") public enum LoggerKind {
        mblog {
            @Override public void apply(PieBuilder builder, SpoofaxPieState spoofaxPieState) {
                Logger logger = spoofaxPieState.logger;
                LogLoggerKt.withMbLogger(builder, logger);
            }
        },
        stdout {
            @Override public void apply(PieBuilder builder, SpoofaxPieState spoofaxPieState) {
                builder.withLogger(StreamLogger.Companion.non_verbose());
            }
        },
        stdout_verbose {
            @Override public void apply(PieBuilder builder, SpoofaxPieState spoofaxPieState) {
                builder.withLogger(StreamLogger.Companion.verbose());
            }
        },
        noop {
            @Override public void apply(PieBuilder builder, SpoofaxPieState spoofaxPieState) {
                builder.withLogger(new NoopLogger());
            }
        };

        public abstract void apply(PieBuilder builder, SpoofaxPieState spoofaxPieState);
    }

    @SuppressWarnings("unused") public enum ExecutorLoggerKind {
        log {
            @Override public void apply(PieBuilder builder) {
                builder.withExecutorLogger(LoggerExecutorLogger::new);
            }
        },
        noop {
            @Override public void apply(PieBuilder builder) {
                builder.withExecutorLogger(logger -> new NoopExecutorLogger());
            }
        };

        public abstract void apply(PieBuilder builder);
    }
}
