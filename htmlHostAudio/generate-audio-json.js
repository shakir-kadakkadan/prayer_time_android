const fs = require('fs');
const path = require('path');

const audioDir = path.join(__dirname, 'public', 'audio');
const baseUrl = 'https://prayer-time-shakir.web.app/audio/';

// Read all audio files
const files = fs.readdirSync(audioDir).filter(file => file.endsWith('.mp3'));

const audioList = files.map(file => {
    const filePath = path.join(audioDir, file);
    const stats = fs.statSync(filePath);
    const encodedFileName = encodeURIComponent(file);

    return {
        name: file.replace('.mp3', ''),
        url: baseUrl + encodedFileName,
        size: stats.size,
        sizeFormatted: formatBytes(stats.size)
    };
});

// Sort by name
audioList.sort((a, b) => a.name.localeCompare(b.name));

// Write to JSON file
const outputPath = path.join(__dirname, 'public', 'audio-list.json');
fs.writeFileSync(outputPath, JSON.stringify(audioList, null, 2));

console.log(`Generated audio-list.json with ${audioList.length} files`);
console.log(JSON.stringify(audioList, null, 2));

function formatBytes(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
}
