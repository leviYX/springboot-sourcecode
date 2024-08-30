package smoketest.simple.controller;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/upload")
public class UploadController {

	private static final String UPLOAD_DIR = "D:\\upload\\";

	@PostMapping("/moreFileUpload")
	public String moreFileUpload(@RequestPart(name = "photos") MultipartFile[] photos) throws IOException {
		if (photos != null) {
			for (MultipartFile photo : photos) {
				if (!photo.isEmpty()) {
					photo.transferTo(new File(UPLOAD_DIR + photo.getOriginalFilename()));
				}
			}
		}
		return "ok";
	}

	@PostMapping("/oneFileUpload")
	public String oneFileUpload(@RequestPart(name = "photo") MultipartFile photo) throws IOException {
		if (!photo.isEmpty()) {
			photo.transferTo(new File(UPLOAD_DIR + photo.getOriginalFilename()));
		}
		return "ok";
	}
}
