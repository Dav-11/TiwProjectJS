package it.polimi.tiw.tiwprojectjs.controllers;

import it.polimi.tiw.tiwprojectjs.beans.Auction;
import it.polimi.tiw.tiwprojectjs.beans.Item;
import it.polimi.tiw.tiwprojectjs.beans.User;
import it.polimi.tiw.tiwprojectjs.dao.AuctionDAO;
import it.polimi.tiw.tiwprojectjs.dao.ItemDAO;
import it.polimi.tiw.tiwprojectjs.utils.ConnectionHandler;
import org.apache.commons.lang3.StringEscapeUtils;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet(name = "CreateAuction", value = "/CreateAuction")
@MultipartConfig
public class CreateAuction extends HttpServlet {

    private Connection connection =null;

    public void init() throws ServletException{

        connection = ConnectionHandler.getConnection(getServletContext());
    }

    /**
     * params:
     * itemName -> required, max 45
     * itemDescription -> max 500
     * itemPicture
     * end_date -> required, must be future
     * initial_price -> required, >= 0
     * min_rise -> required, >=0
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        if (session.isNew() || session.getAttribute("user") == null) {

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().println("unauthorized user");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        String itemName = null;
        String itemDescription = null;
        Part itemPicturePart = null;
        InputStream itemPicture = null;
        Date end_date = null;
        float min_rise = -1;
        float initial_price = -1;

        try{

            itemName = StringEscapeUtils.escapeJava(request.getParameter("itemName"));
            itemDescription = ( request.getParameter("itemDescription").isEmpty() ) ? null : StringEscapeUtils.escapeJava(request.getParameter("itemDescription"));
            itemPicturePart = request.getPart("itemPicture");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            end_date = (Date) sdf.parse(request.getParameter("end_date"));
            initial_price = Float.parseFloat(request.getParameter("initial_price"));
            min_rise = Float.parseFloat(request.getParameter("min_rise"));

            System.out.println("itmName: " + itemName + " description:" + itemDescription + " picture: " + itemPicture + " end_date:" + end_date + " min_rise:" + min_rise + " initial_price:" + initial_price);

        } catch (NumberFormatException | NullPointerException | ParseException e){

            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Incorrect or missing param values");
            return;
        }

        if ( (itemName == null) || (itemName.isEmpty()) ){

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Item Name must be specified");
            return;
        }

        if (itemName.length() > 45){

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Item Name must be less than 45 char long");
            return;
        }

        if (end_date == null){

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("End Date must be specified");
            return;
        }

        if (! end_date.after(new Date())){

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Date must be in the future");
            return;
        }

        if (min_rise < 0){

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("The Minimum rise must be >= 0");
            return;
        }

        if (initial_price < 0){

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Initial price must be >= 0");
            return;
        }

        if ( (itemDescription != null) && ( (itemDescription.length() >500) || (itemDescription.isEmpty()) ) ){

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("The Item description cannot be empty or longer than 500 char");
            return;
        }

        if ((itemPicturePart != null) && (itemPicturePart.getSize() > 0)){

            String contentType = itemPicturePart.getContentType();
            System.out.println("File contentType: " + contentType);
            if (! contentType.startsWith("image")){

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("File must be of type jpeg");
                return;
            }

            itemPicture = itemPicturePart.getInputStream();
        }

        // create a new auction bean and saves it
        AuctionDAO auctionDAO = new AuctionDAO(connection);
        Auction auction = new Auction(
                currentUser.getId(),
                new Date(),
                end_date,
                min_rise,
                initial_price,
                true
        );

        try {
            auction.setId(auctionDAO.createAuction(auction));
        } catch (SQLException e){

            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Not possible to create auction");
            return;
        }

        // create a new item and saves it
        ItemDAO itemDAO = new ItemDAO(connection);
        Item item = new Item(
                itemName,
                itemDescription,
                itemPicture,
                auction.getId()
        );

        try {
            item.setId(itemDAO.createItem(item));
        } catch (SQLException e){

            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Not possible to create item");
            return;
        }

        // send answer
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println("Auction Created");
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
