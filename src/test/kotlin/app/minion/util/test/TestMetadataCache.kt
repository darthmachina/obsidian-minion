package app.minion.util.test

import CachedMetadata
import MetadataCache

class TestMetadataCache(val cachedMetadata: CachedMetadata) : MetadataCache() {
    /**
     * Always return cachedMetadata passed in
     */
    override fun getCache(path: String): CachedMetadata? {
        return cachedMetadata
    }
}