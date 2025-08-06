package it.polimi.auction.controller;

import it.polimi.auction.DBUtil;
import it.polimi.auction.beans.User;
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
import java.sql.SQLException;
import java.util.UUID;

@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,  // 1MB
        maxFileSize = 1024 * 1024 * 10,   // 10MB
        maxRequestSize = 1024 * 1024 * 50 // 50MB
)


@WebServlet("/sell")
public class sellServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) request.getSession().getAttribute("user");
        // serve per mettere in database indirizzo del file nel disco
        String appPath = request.getServletContext().getRealPath("/");
        int user_id = user.getId();


        String song = request.getParameter("song");
        String artist = request.getParameter("artist");
        Part mp3Part = request.getPart("audio");

        //if title is empty or artist is empty or mp3Part is empty, return error
        if (song.isEmpty() || artist.isEmpty() || mp3Part.getSize() == 0) {
            request.setAttribute("error", "Required fields are empty");
            request.getRequestDispatcher("/error").forward(request, response);
            return;
        }


        String album = request.getParameter("album");
        String yearStr = request.getParameter("year");
        Integer year;
        if (yearStr != null && !yearStr.trim().isEmpty()) {
            year = Integer.parseInt(yearStr);
        } else {
            year = null;
        }
        String genre = request.getParameter("genre");
        if(genre.isEmpty()){
            genre = null;
        }
        if(album.isEmpty()){
            album = "Unknown";
        }

        Path mp3Path = null;
        Path coverPath;

        try {
            SongDAO songDAO = new SongDAO(DBUtil.getConnection());
            if(songDAO.checkIfSongExists(user_id, song, artist)){
                throw new SQLException("Song already exists");
            }

            //image
            Part coverPart = request.getPart("cover");

            // increase security
            validateFileType(mp3Part, "audio/");
            if (coverPart.getSize() > 0) {
                validateFileType(coverPart, "image/");
            }

            String mp3FileName = generateSafeFileName(mp3Part);
            String coverFileName = (coverPart.getSize() > 0) ?
                    generateSafeFileName(coverPart) : null;
            Path userUploadDir  = Paths.get(appPath, "uploads", "user_" + user_id);

            try {
                Files.createDirectories(userUploadDir);
            } catch (IOException e) {
                throw new ServletException("Can't create upload directory", e);
            }

            //aggiungere davanti a mp3FileName il indirizzo del file
            mp3Path = userUploadDir.resolve(mp3FileName);

            if (coverFileName != null) {
                coverPath = userUploadDir.resolve(coverFileName);
                saveFile(coverPart, coverPath.toString());
            }

            //save relatives paths in database
            String relativeMp3Path = "/uploads/user_" + user.getId() + "/" + mp3FileName;
            String relativeCoverPath = (coverFileName != null) ?
                    "/uploads/user_" + user.getId() + "/" + coverFileName : null;

            //ritorna numero di riga effected
            int result = songDAO.uploadSong(user_id, song, artist, album, year, genre, relativeMp3Path, relativeCoverPath);

            if (result > 0) {
                saveFile(mp3Part, mp3Path.toString());
                request.getRequestDispatcher("/home").forward(request, response);
            }


        } catch (Exception e) {
            if (mp3Path != null) {
                Files.deleteIfExists(mp3Path);
            }
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/error").forward(request, response);
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) request.getSession().getAttribute("user");
        // serve per mettere in database indirizzo del file nel disco
        String appPath = request.getServletContext().getRealPath("/");
        int user_id = user.getId();


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