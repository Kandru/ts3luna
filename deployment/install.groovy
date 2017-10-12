import groovy.io.FileType

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

installDir = Paths.get(getClass().protectionDomain.codeSource.location.toURI()).parent
applicationDir = installDir.resolve("application")
applicationJar = applicationDir.resolve("ts3luna.jar")
installConfig = applicationDir.resolve("application.yml")
homeDir = Paths.get(System.getProperty("user.home"))
lunaDir = homeDir.resolve("ts3luna")
lunaConfig = lunaDir.resolve("application.yml")
lunaBackupDir = homeDir.resolve("ts3luna-backups")
newBackup = lunaBackupDir.resolve("${new Date().format('dd-MM-yyyy_hh-mm-ss')}.zip")

stopApplication()
backupCurrentInstallation()
installApplication()
startApplication()

void backupCurrentInstallation() {
    Files.createDirectories(lunaBackupDir)

    def backupStream = new ZipOutputStream(newBackup.newOutputStream())
    addToZip("", lunaDir, backupStream)
    backupStream.close()
}

void addToZip(String prefix, Path path, ZipOutputStream out) {
    path.eachFile { Path file ->
        if (Files.isDirectory(file)) {
            addToZip("${prefix}${file.toFile().name}/", file, out)
            return
        }
        out.putNextEntry(new ZipEntry("${prefix}${file.toFile().name}"))
        file.withInputStream { input ->
            Files.copy(input, out)
        }
    }
}

void installApplication() {
    lunaDir.eachFileRecurse(FileType.FILES) {
        if( it.fileName ==~ '.*\\.yml' || it.fileName ==~ '.*start\\.sh' || it.fileName ==~ '.*stop\\.sh' || it.fileName ==~ '.*\\.xml' ) { return }
        Files.delete it
    }

    def delete = []
    lunaDir.eachDirRecurse {
        if(!it.toFile().list()) {
            delete += [it]
        }
    }
    delete.each { Files.delete(it) }

    Files.copy(applicationJar, lunaDir.resolve(applicationJar.fileName))

    if(!Files.exists(lunaConfig)) {
        Files.copy(installConfig, lunaConfig)
    }
}

void stopApplication() {
    "$lunaDir/stop.sh".execute()
}

void startApplication() {
    "$lunaDir/start.sh".execute()
}






