package it.polimi.auction.controller;

import it.polimi.auction.DBUtil;
import it.polimi.auction.beans.User;
import it.polimi.auction.dao.ItemDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import static it.polimi.auction.Util.requireParameter;

@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,  // 1MB
        maxFileSize = 1024 * 1024 * 10,   // 10MB
        maxRequestSize = 1024 * 1024 * 50 // 50MB
)


@WebServlet("/uploadItem")
public class UploadItemServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) request.getSession().getAttribute("user");
        // serve per mettere in database indirizzo del file nel disco
        int user_id = user.getId();
        String uploadBaseDir = getServletContext().getInitParameter("upload.dir");
        Path userUploadDir = Paths.get(uploadBaseDir, "user_" + user_id);
//      String appPath = request.getServletContext().getRealPath("/");

        String item;
        String description;
        String priceStr;

        try{
            item = requireParameter(request, "item");
            description = requireParameter(request, "description");
            priceStr = requireParameter(request, "price");
        }catch (IllegalArgumentException e){
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/sell").forward(request, response);
            return;
        }
        Part coverPart;
        try{
            coverPart = request.getPart("cover");
        }catch (IOException | ServletException e){
            request.setAttribute("error", "Error while uploading cover");
            request.getRequestDispatcher("/sell").forward(request, response);
            return;
        }
        //if title is empty or artist is empty or mp3Part is empty, return error
        if (coverPart.getSize() == 0) {
            request.setAttribute("error", "Required fields are empty");
            request.getRequestDispatcher("/sell").forward(request, response);
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
                request.setAttribute("error", "Error while uploading cover");
                request.getRequestDispatcher("/sell").forward(request, response);
                return;
            }
            saveFile(coverPart, coverPath.toString());
            //save relatives paths in database
            String relativeCoverPath = "/uploads/user_" + user.getId() + "/" + coverFileName;

            try {
                double price = Double.parseDouble(priceStr);
                //ritorna numero di riga effected
                int result =itemDAO.uploadItem(item, description, relativeCoverPath, price, user_id);

                if (result > 0) {
                    request.getRequestDispatcher("/sell").forward(request, response);
                }
            } catch (NumberFormatException e) {
                request.setAttribute("error", "Price must be a number");
                request.getRequestDispatcher("/sell").forward(request, response);
            }

        } catch (Exception e) {
            if (coverPath != null) {
                Files.deleteIfExists(coverPath);
            }
            request.setAttribute("error", "caricamento no riuscito");
            request.getRequestDispatcher("/sell").forward(request, response);
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