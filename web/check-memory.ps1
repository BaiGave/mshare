# Check for MinecraftScreenCapture shared memory
$memoryName = "Global\MinecraftScreenCapture"

try {
    $map = [System.IO.MemoryMappedFiles.MemoryMappedFile]::OpenExisting($memoryName, "Read")
    if ($map) {
        Write-Output "FOUND: $memoryName"
        $map.Dispose()
    }
} catch {
    # Try without Global prefix
    try {
        $map = [System.IO.MemoryMappedFiles.MemoryMappedFile]::OpenExisting("MinecraftScreenCapture", "Read")
        if ($map) {
            Write-Output "FOUND: MinecraftScreenCapture"
            $map.Dispose()
        }
    } catch {
        Write-Output "NOTFOUND"
    }
}
