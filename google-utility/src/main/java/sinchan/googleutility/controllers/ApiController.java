package sinchan.googleutility.controllers;

import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.api.services.photoslibrary.v1.model.Album;
import com.google.api.services.photoslibrary.v1.model.MediaItem;
import sinchan.googleutility.service.GooglePhotosService;

@RestController
public class ApiController {
    @Autowired
    @org.springframework.context.annotation.Lazy
    private GooglePhotosService googlePhotosService;

    @GetMapping("/apis/test")
    public String test(){
       return "Process is deployed";
    }

    @GetMapping("/apis/albums")
    public List<Album> listAlbums() throws IOException {
        return googlePhotosService.listAlbums();
    }

    @GetMapping("/apis/albums/{id}/photos")
    public List<MediaItem> listPhotos(@PathVariable("id") String albumId) throws IOException {
        return googlePhotosService.listPhotos(albumId);
    }

    @PostMapping("/apis/albums/{id}/download")
    public String downloadAlbum(@PathVariable("id") String albumId) throws IOException {
        googlePhotosService.downloadAlbum(albumId);
        return "Download started for album " + albumId;
    }
}
