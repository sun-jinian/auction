package it.polimi.auction.controller;

import com.google.gson.Gson;
import it.polimi.auction.DBUtil;
import it.polimi.auction.beans.User;
import it.polimi.auction.dao.ItemDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,  // 1MB
        maxFileSize = 1024 * 1024 * 10,   // 10MB
        maxRequestSize = 1024 * 1024 * 50 // 50MB
)


@WebServlet("/uploadItem")
public class UploadItemServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> responseData = new HashMap<>();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if(session.getAttribute("user") instanceof User user) {
            // serve per mettere in database indirizzo del file nel disco
            int user_id = user.getId();
            String uploadBaseDir = getServletContext().getInitParameter("upload.dir");
            Path userUploadDir = Paths.get(uploadBaseDir, "user_" + user_id);


            String item = request.getParameter("item");
            String description = request.getParameter("description");
            String priceStr = request.getParameter("price");
            Part coverPart = request.getPart("cover");
            //if title is empty or artist is empty or mp3Part is empty, return error
            if (item.isEmpty() || description.isEmpty() || coverPart.getSize() == 0 || priceStr.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseData.put("error", "Required fields is missing");
                response.getWriter().write(gson.toJson(responseData));
                return;
            }

            Path coverPath = null;

            try {
                ItemDAO itemDAO = new ItemDAO(DBUtil.getConnection());
                if (coverPart.getSize() > 0) {
                    validateFileType(coverPart, "image/");
                }
                String coverFileName = generateSafeFileName(coverPart);

                coverPath = userUploadDir.resolve(coverFileName);

                try {
                    Files.createDirectories(userUploadDir);
                } catch (IOException e) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    responseData.put("error", "server error");
                    response.getWriter().write(gson.toJson(responseData));
                }
                saveFile(coverPart, coverPath.toString());
                //save relatives paths in database
                String relativeCoverPath = "/uploads/user_" + user.getId() + "/" + coverFileName;

                try {
                    double price = Double.parseDouble(priceStr);
                    //ritorna numero di riga effected
                    int result =itemDAO.uploadItem(item, description, relativeCoverPath, price, user_id);

                    if (result > 0) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        responseData.put("message", "Item uploaded successfully");
                        response.getWriter().write(gson.toJson(responseData));
                    }
                } catch (SQLException e) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    responseData.put("error", "database connection error");
                    response.getWriter().write(gson.toJson(responseData));
                }

            } catch (IllegalArgumentException e) {
                if (coverPath != null) {
                    Files.deleteIfExists(coverPath);
                }
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseData.put("error", e.getMessage());
                response.getWriter().write(gson.toJson(responseData));
            } catch (Exception e) {
                if (coverPath != null) {
                    Files.deleteIfExists(coverPath);
                }
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                responseData.put("error", "upload item failed in server side");
                response.getWriter().write(gson.toJson(responseData));
            }
        }

    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }

    //serve per generate un file name randomizzato per evitare collisioni
    private String generateSafeFileName(Part part) {
        String ext = getFileExtension(part.getSubmittedFileName());
        return UUID.randomUUID() + ext;
    }

    private void validateFileType(Part part, String expectedType) {
        String contentType = part.getContentType();
        if (!contentType.startsWith(expectedType)) {
            throw new IllegalArgumentException("Invalid file type: " + contentType);
        }
    }

    private void saveFile(Part filePart, String filePath) throws IOException {
        try (InputStream input = filePart.getInputStream()) {
            Files.copy(input, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}