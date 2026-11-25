package com.halil.sonsuzyolarkadasi

import android.content.Context
import android.os.Environment
import java.io.File

class SDCardModelManager(private val context: Context) {

    fun findExternalModelDirectory(): File? {
        val externalDirs = context.getExternalFilesDirs(null)
        for (dir in externalDirs) {
            if (dir != null && Environment.isExternalStorageRemovable(dir)) {
                val sdRoot = dir.absolutePath.substringBefore("/Android")
                val modelFolder = File(sdRoot, "SonsuzYolArkadasi/models")
                if (!modelFolder.exists()) {
                    modelFolder.mkdirs()
                }
                return modelFolder
            }
        }
        val fallback = File(Environment.getExternalStorageDirectory(), "SonsuzYolArkadasi/models")
        if (!fallback.exists()) fallback.mkdirs()
        return fallback
    }

    fun listAvailableModels(): List<File> {
        val dir = findExternalModelDirectory() ?: return emptyList()
        return dir.listFiles { file ->
            file.isFile && (file.name.endsWith(".gguf") || file.name.endsWith(".bin"))
        }?.toList() ?: emptyList()
    }
}
