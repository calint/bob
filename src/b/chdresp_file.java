// reviewed: 2024-08-05
package b;

import java.nio.ByteBuffer;

/** Cached file. */
final class chdresp_file extends chdresp {
    private final path path;
    private long last_modified;
    private long last_validation_time;

    /** Caches a file. */
    chdresp_file(final path p, final byte[] content_type) throws Throwable {
        path = p;
        this.content_type = content_type;
        if (!validate(System.currentTimeMillis())) {
            throw new RuntimeException();
        }
    }

    /** @return true if path is valid, false to evict it from the cache */
    @Override
    boolean validate(final long now) throws Throwable {
        if (now - last_validation_time < b.cache_files_validate_dt) {
            return true;
        }
        last_validation_time = now;
        if (!path.exists()) {
            // file is gone
            return false;
        }
        final long path_last_modified = path.lastmod();
        if (path_last_modified == last_modified) {
            // file is up to date
            return true;
        }
        // file needs to be refreshed
        if (path.size() > b.cache_files_maxsize) {
            // file has changed and is now to big
            return false;
        }
        content_length = (int) path.size();
        // note: assumes less than 4 GB
        // build cached buffers
        bb = ByteBuffer.allocateDirect(hdrlencap + content_length);
        bb.put(req.h_http200);
        bb.put(req.h_content_length).put(Long.toString(content_length).getBytes());
        if (content_type != null) {
            bb.put(req.h_content_type).put(content_type);
        }
        etag = "\"" + (path_last_modified / 1000) * 1000 + "\"";
        // note. java 21 uses millis in timestamp while java 5 in seconds
        bb.put(req.h_etag).put(etag.getBytes());
        bb.put(req.hkp_connection_keep_alive);
        additional_headers_insertion_position = bb.position();
        bb.put(req.ba_crlf2);
        content_position = bb.position();
        path.to(bb);
        bb.flip();
        last_modified = path_last_modified;
        return true;
    }
}
