package co.za.neighborlygigs.service;

import co.za.neighborlygigs.domain.User;
import org.springframework.web.multipart.MultipartFile;

// Extends generic CRUD + adds custom methods
public interface UserService extends IService<User, Long> {
    User updateProfile(Long userId, String bio, String phone);
    String uploadProfilePicture(Long userId, MultipartFile file);
    String uploadCv(Long userId, MultipartFile file);
}
