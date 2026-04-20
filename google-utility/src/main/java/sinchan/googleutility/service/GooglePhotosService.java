package sinchan.googleutility.service;

import com.google.api.services.photoslibrary.v1.PhotosLibrary;
import com.google.api.services.photoslibrary.v1.model.Album;
import com.google.api.services.photoslibrary.v1.model.MediaItem;
import com.google.api.services.photoslibrary.v1.model.ListAlbumsResponse;
import com.google.api.services.photoslibrary.v1.model.SearchMediaItemsRequest;
import com.google.api.services.photoslibrary.v1.model.SearchMediaItemsResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Service
public class GooglePhotosService {
    private final PhotosLibrary photosLibrary;
    private final Path downloadDir;
    private boolean initialized = false;

    public GooglePhotosService() {
        this.downloadDir = Path.of("downloads");
        try {
            Files.createDirectories(this.downloadDir);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create download directory", e);
        }
        this.photosLibrary = null;
    }

    private void initializeIfNeeded() {
        if (initialized) return;
        String clientId = System.getenv("GOOGLE_CLIENT_ID");
        String clientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
        if (clientId == null || clientSecret == null) {
            throw new IllegalStateException("GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET env vars must be set");
        }
        this.initialized = true;
    }

    public List<Album> listAlbums() throws IOException {
        initializeIfNeeded();
        List<Album> allAlbums = new ArrayList<>();
        if (photosLibrary == null) {
            return allAlbums;
        }
        String nextPageToken = null;
        do {
            PhotosLibrary.Albums.List request = photosLibrary.albums().list();
            request.setPageToken(nextPageToken);
            ListAlbumsResponse response = request.execute();
            if (response.getAlbums() != null) {
                allAlbums.addAll(response.getAlbums());
            }
            nextPageToken = response.getNextPageToken();
        } while (nextPageToken != null);
        return allAlbums;
    }

    public List<MediaItem> listPhotos(String albumId) throws IOException {
        initializeIfNeeded();
        List<MediaItem> allItems = new ArrayList<>();
        if (photosLibrary == null) {
            return allItems;
        }
        String nextPageToken = null;
        do {
            SearchMediaItemsRequest searchRequest = new SearchMediaItemsRequest()
                    .setAlbumId(albumId)
                    .setPageToken(nextPageToken);
            PhotosLibrary.MediaItems.Search request = photosLibrary.mediaItems().search(searchRequest);
            SearchMediaItemsResponse response = request.execute();
            if (response.getMediaItems() != null) {
                allItems.addAll(response.getMediaItems());
            }
            nextPageToken = response.getNextPageToken();
        } while (nextPageToken != null);
        return allItems;
    }

    public void downloadAlbum(String albumId) throws IOException {
        initializeIfNeeded();
        List<MediaItem> items = listPhotos(albumId);
        for (MediaItem item : items) {
            String baseUrl = item.getBaseUrl();
            if (baseUrl == null) continue;
            URL downloadUrl = new URL(baseUrl + "=d");
            String mimeType = item.getMimeType() != null ? item.getMimeType().replace("/", ".") : "bin";
            String filename = item.getId() + "_" + mimeType;
            try (InputStream in = downloadUrl.openStream()) {
                Path target = downloadDir.resolve(filename);
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}