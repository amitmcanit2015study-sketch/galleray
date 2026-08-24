package com.amitbharat.gallery.data.models;

import java.io.Serializable;

public class ExifInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean isVideo = false;
    private String fileName;
    private String filePath;
    private String fileDirectory;
    private long fileSize;
    private String mimeType;
    private String resolution;
    private String megapixels;
    private String dateTaken;
    private String dateModified;
    private String cameraMake;
    private String cameraModel;
    private String lensModel;
    private String software;
    private String aperture;
    private String exposureTime;
    private String iso;
    private String focalLength;
    private String focalLength35mm;
    private String exposureProgram;
    private String exposureBias;
    private String meteringMode;
    private String flash;
    private String whiteBalance;
    private String colorSpace;
    private String orientation;
    private String gpsLatitude;
    private String gpsLongitude;
    private String gpsAltitude;
    private String locationText;

    // Video specific metadata
    private String duration;
    private String bitrate;
    private String frameRate;
    private String videoCodec;
    private String audioCodec;

    public ExifInfo() {}

    public boolean isVideo() { return isVideo; }
    public void setVideo(boolean video) { isVideo = video; }

    public String getFileName() { return fileName != null ? fileName : ""; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath != null ? filePath : ""; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileDirectory() { return fileDirectory != null ? fileDirectory : ""; }
    public void setFileDirectory(String fileDirectory) { this.fileDirectory = fileDirectory; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getMimeType() { return mimeType != null ? mimeType : ""; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getResolution() { return resolution != null ? resolution : "--"; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    public String getMegapixels() { return megapixels != null ? megapixels : ""; }
    public void setMegapixels(String megapixels) { this.megapixels = megapixels; }

    public String getDateTaken() { return dateTaken != null ? dateTaken : "--"; }
    public void setDateTaken(String dateTaken) { this.dateTaken = dateTaken; }

    public String getDateModified() { return dateModified != null ? dateModified : "--"; }
    public void setDateModified(String dateModified) { this.dateModified = dateModified; }

    public String getCameraMake() { return cameraMake != null ? cameraMake : ""; }
    public void setCameraMake(String cameraMake) { this.cameraMake = cameraMake; }

    public String getCameraModel() { return cameraModel != null ? cameraModel : "--"; }
    public void setCameraModel(String cameraModel) { this.cameraModel = cameraModel; }

    public String getLensModel() { return lensModel != null ? lensModel : ""; }
    public void setLensModel(String lensModel) { this.lensModel = lensModel; }

    public String getSoftware() { return software != null ? software : ""; }
    public void setSoftware(String software) { this.software = software; }

    public String getAperture() { return aperture != null ? aperture : "--"; }
    public void setAperture(String aperture) { this.aperture = aperture; }

    public String getExposureTime() { return exposureTime != null ? exposureTime : "--"; }
    public void setExposureTime(String exposureTime) { this.exposureTime = exposureTime; }

    public String getIso() { return iso != null ? iso : "--"; }
    public void setIso(String iso) { this.iso = iso; }

    public String getFocalLength() { return focalLength != null ? focalLength : "--"; }
    public void setFocalLength(String focalLength) { this.focalLength = focalLength; }

    public String getFocalLength35mm() { return focalLength35mm != null ? focalLength35mm : ""; }
    public void setFocalLength35mm(String focalLength35mm) { this.focalLength35mm = focalLength35mm; }

    public String getExposureProgram() { return exposureProgram != null ? exposureProgram : ""; }
    public void setExposureProgram(String exposureProgram) { this.exposureProgram = exposureProgram; }

    public String getExposureBias() { return exposureBias != null ? exposureBias : ""; }
    public void setExposureBias(String exposureBias) { this.exposureBias = exposureBias; }

    public String getMeteringMode() { return meteringMode != null ? meteringMode : ""; }
    public void setMeteringMode(String meteringMode) { this.meteringMode = meteringMode; }

    public String getFlash() { return flash != null ? flash : "--"; }
    public void setFlash(String flash) { this.flash = flash; }

    public String getWhiteBalance() { return whiteBalance != null ? whiteBalance : "--"; }
    public void setWhiteBalance(String whiteBalance) { this.whiteBalance = whiteBalance; }

    public String getColorSpace() { return colorSpace != null ? colorSpace : ""; }
    public void setColorSpace(String colorSpace) { this.colorSpace = colorSpace; }

    public String getOrientation() { return orientation != null ? orientation : ""; }
    public void setOrientation(String orientation) { this.orientation = orientation; }

    public String getGpsLatitude() { return gpsLatitude != null ? gpsLatitude : ""; }
    public void setGpsLatitude(String gpsLatitude) { this.gpsLatitude = gpsLatitude; }

    public String getGpsLongitude() { return gpsLongitude != null ? gpsLongitude : ""; }
    public void setGpsLongitude(String gpsLongitude) { this.gpsLongitude = gpsLongitude; }

    public String getGpsAltitude() { return gpsAltitude != null ? gpsAltitude : ""; }
    public void setGpsAltitude(String gpsAltitude) { this.gpsAltitude = gpsAltitude; }

    public String getLocationText() { return locationText != null ? locationText : "No GPS data"; }
    public void setLocationText(String locationText) { this.locationText = locationText; }

    public String getDuration() { return duration != null ? duration : "--"; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getBitrate() { return bitrate != null ? bitrate : "--"; }
    public void setBitrate(String bitrate) { this.bitrate = bitrate; }

    public String getFrameRate() { return frameRate != null ? frameRate : "--"; }
    public void setFrameRate(String frameRate) { this.frameRate = frameRate; }

    public String getVideoCodec() { return videoCodec != null ? videoCodec : "--"; }
    public void setVideoCodec(String videoCodec) { this.videoCodec = videoCodec; }

    public String getAudioCodec() { return audioCodec != null ? audioCodec : "--"; }
    public void setAudioCodec(String audioCodec) { this.audioCodec = audioCodec; }
}
