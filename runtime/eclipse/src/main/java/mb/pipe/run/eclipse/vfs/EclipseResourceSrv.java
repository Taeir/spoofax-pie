package mb.pipe.run.eclipse.vfs;

import java.io.File;
import java.net.URI;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.spoofax.eclipse.resource.EclipseResourceFileObject;

import com.google.inject.Inject;

import mb.pipe.run.core.log.Logger;
import mb.pipe.run.core.path.FileUtils;
import mb.pipe.run.core.path.PPath;
import mb.pipe.run.core.path.PathSrvImpl;
import mb.pipe.run.eclipse.util.Nullable;

public class EclipseResourceSrv extends PathSrvImpl implements IEclipseResourceSrv {
    private final Logger logger;
    private final IWorkspaceRoot root;


    @Inject public EclipseResourceSrv(Logger logger, FileSystemManager fileSystemManager, FileSystemOptions options) {
        super(logger, fileSystemManager, options);
        this.logger = logger.forContext(getClass());
        this.root = ResourcesPlugin.getWorkspace().getRoot();
    }


    @Override public PPath resolve(org.eclipse.core.resources.IResource resource) {
        return resolve(resource.getFullPath());
    }

    @Override public PPath resolve(IPath path) {
        return resolve("eclipse://" + path.toString());
    }

    @Override public PPath resolveWorkspaceRoot() {
        return resolve(root);
    }

    @Override public @Nullable PPath resolve(IEditorInput input) {
        if(input instanceof IFileEditorInput) {
            final IFileEditorInput fileInput = (IFileEditorInput) input;
            return resolve(fileInput.getFile());
        } else if(input instanceof IPathEditorInput) {
            final IPathEditorInput pathInput = (IPathEditorInput) input;
            return resolve(pathInput.getPath());
        } else if(input instanceof IURIEditorInput) {
            final IURIEditorInput uriInput = (IURIEditorInput) input;
            return resolve(uriInput.getURI());
        } else if(input instanceof IStorageEditorInput) {
            final IStorageEditorInput storageInput = (IStorageEditorInput) input;
            final IStorage storage;
            try {
                storage = storageInput.getStorage();
            } catch(CoreException e) {
                return null;
            }

            final IPath path = storage.getFullPath();
            if(path != null) {
                return resolve(path);
            } else {
                try {
                    final PPath ramFile = resolve("ram://eclipse/" + input.getName());
                    return ramFile;
                } catch(MetaborgRuntimeException e) {
                    return null;
                }
            }
        }
        logger.error("Could not resolve editor input {}", input);
        return null;
    }


    @Override public @Nullable org.eclipse.core.resources.IResource unresolve(PPath resource) {
        final FileObject vfsFile = resource.fileObject();
        if(vfsFile instanceof EclipseFileObject) {
            final EclipseFileObject eclipseResource = (EclipseFileObject) vfsFile;
            try {
                return eclipseResource.resource();
            } catch(Exception e) {
                logger.error("Could not unresolve resource {} to an Eclipse resource", e, resource);
                return null;
            }
        }

        if(vfsFile instanceof EclipseResourceFileObject) {
            final EclipseResourceFileObject eclipseResource = (EclipseResourceFileObject) vfsFile;
            try {
                return eclipseResource.resource();
            } catch(Exception e) {
                logger.error("Could not unresolve resource {} to an Eclipse resource", e, resource);
                return null;
            }
        }

        if(vfsFile instanceof LocalFile) {
            // LEGACY: analysis returns messages with relative local file resources, try to convert as relative first.
            final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            final String path = vfsFile.getName().getPath();
            org.eclipse.core.resources.IResource eclipseResource = root.findMember(path);
            if(eclipseResource == null) {
                // Path might be absolute, try to get absolute file.
                final URI uri = FileUtils.toURI(vfsFile);
                final IPath location = Path.fromOSString(uri.getPath());
                eclipseResource = root.getFileForLocation(location);
                if(eclipseResource == null) {
                    // If resource is a direct path to a project, getContainerForLocation needs to be used.
                    eclipseResource = root.getContainerForLocation(location);
                }
            }
            return eclipseResource;
        }

        return null;
    }

    @Override public File localFile(PPath resource) {
        // TODO: remove conversion to FileObject
        final FileObject vfsFile = resource.fileObject();
        if(!(vfsFile instanceof EclipseFileObject) && !(vfsFile instanceof EclipseResourceFileObject)) {
            return super.localFile(resource);
        }

        final File localResource = localPath(resource);
        if(localResource == null) {
            return super.localFile(resource);
        }
        return super.localFile(resolve(localResource));
    }

    @Override public File localFile(PPath resource, PPath dir) {
        // TODO: remove conversion to FileObject
        final FileObject vfsFile = resource.fileObject();
        final FileObject vfsDir = dir.fileObject();
        if((!(vfsDir instanceof EclipseFileObject) && !(vfsDir instanceof EclipseResourceFileObject))
            || (!(vfsFile instanceof EclipseFileObject) && !(vfsFile instanceof EclipseResourceFileObject))) {
            return super.localFile(resource, dir);
        }

        final File localResource = localPath(resource);
        if(localResource == null) {
            return super.localFile(resource, dir);
        }
        final File localDir = localPath(dir);
        if(localDir == null) {
            return super.localFile(resource, dir);
        }

        return super.localFile(resolve(localResource), resolve(localDir));
    }

    @Override public @Nullable File localPath(PPath resource) {
        // TODO: remove conversion to FileObject
        final FileObject vfsFile = resource.fileObject();

        if(!(vfsFile instanceof EclipseFileObject) && !(vfsFile instanceof EclipseResourceFileObject)) {
            return super.localPath(resource);
        }

        try {
            final org.eclipse.core.resources.IResource eclipseResource = unresolve(resource);
            IPath path = eclipseResource.getRawLocation();
            if(path == null) {
                path = eclipseResource.getLocation();
            }
            if(path == null) {
                return null;
            }
            return path.makeAbsolute().toFile();
        } catch(Exception e) {
            return null;
        }
    }
}
