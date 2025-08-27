package it.polimi.auction.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/uploads/*")
public class ImageServlet extends HttpServlet {

    //this is the directory where the uploaded images are stored
    private static final String base_dir = "D:/opt/auction/uploads";

    /**
     * Handles HTTP GET requests for images in the uploads directory.
     * @param req HTTP request
     * @param resp HTTP response
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Get the extra path info from the request URL.
        // For example, if the servlet is mapped to /files/* and the request is /files/images/pic.jpg,
        // path will be "/images/pic.jpg".
        String path = req.getPathInfo();

        // Create a File object by combining the base directory and the requested path.
        // base_dir is the root folder on the server where files are stored.
        File file = new File(base_dir, path);

        // Check if the requested file exists on the server.
        if (!file.exists()) {
            // If not found, set HTTP response status to 404 (Not Found) and return.
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Set the response MIME type based on the file's name (extension).
        // This helps the browser know how to handle/display the file.
        resp.setContentType(getServletContext().getMimeType(file.getName()));

        // Use try-with-resources to automatically close streams.
        try (FileInputStream in = new FileInputStream(file);
             OutputStream out = resp.getOutputStream()) {

            // Copy all bytes from the file input stream to the response output stream.
            // This sends the file content to the client.
            in.transferTo(out);
        }
    }
}