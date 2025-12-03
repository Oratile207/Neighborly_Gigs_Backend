package co.za.neighborlygigs.service;

import co.za.neighborlygigs.domain.User;
import co.za.neighborlygigs.repository.UserRepository;
import co.za.neighborlygigs.util.FileUploadUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User create(User user) {
        return userRepository.save(user);
    }

    @Override
    public User read(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User update(User user) {
        return userRepository.save(user);
    }

    @Override
    public boolean delete(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Custom methods
    @Override
    public User updateProfile(Long userId, String bio, String phone) {
        User user = read(userId);
        user.setBio(bio);
        user.setPhone(phone);
        return update(user);
    }

    @Override
    public String uploadProfilePicture(Long userId, MultipartFile file) {
        if (!FileUploadUtil.isImageFile(file)) {
            throw new RuntimeException("Only image files allowed");
        }
        // TODO: Save file, return URL
        return "https://neighborlygigs.co.za/images/" + userId + "/profile.jpg";
    }

    @Override
    public String uploadCv(Long userId, MultipartFile file) {
        if (!FileUploadUtil.isDocumentFile(file)) {
            throw new RuntimeException("Only PDF/DOC files allowed");
        }
        return "https://neighborlygigs.co.za/cvs/" + userId + "/cv.pdf";
    }
}