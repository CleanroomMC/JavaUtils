package com.cleanroommc.javautils.api;

import com.cleanroommc.javautils.spi.JavaProvisioner;

/**
 * Receives progress updates while a {@link JavaProvisioner} downloads a distribution.
 * <p>
 * Registered on a provisioner via {@link JavaProvisioner#onDownload(DownloadListener)}.
 * The provisioner invokes {@link #onProgress(long, long, String)} as each byte arrive when performing the download.
 * Implementations should return quickly and must tolerate being called many times.
 */
@FunctionalInterface
public interface DownloadListener {

    /**
     * A listener that discards every update. Used as the default when none is registered.
     */
    DownloadListener NONE = (downloaded, total, fileName) -> { };

    /**
     * Called as a download makes progress.
     *
     * @param downloaded the number of bytes written so far, starting at {@code 0}
     * @param total      the total size of the download in bytes, or {@code -1} if the server did not report a length
     * @param fileName   the name of the archive being downloaded
     */
    void onProgress(long downloaded, long total, String fileName);

}
