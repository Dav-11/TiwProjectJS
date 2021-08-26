package it.polimi.tiw.tiwprojectjs.controllers;

import com.google.gson.Gson;
import it.polimi.tiw.tiwprojectjs.beans.DashboardAuction;
import it.polimi.tiw.tiwprojectjs.beans.User;
import it.polimi.tiw.tiwprojectjs.dao.DashboardAuctionDAO;
import it.polimi.tiw.tiwprojectjs.utils.ConnectionHandler;
import org.apache.commons.lang3.StringEscapeUtils;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "GetAuctionData", value = "/GetAuctionData")
public class GetAuctionData extends HttpServlet {

    private Connection connection = null;

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    /**
     * params
     * type -> required, "OPEN" | "CLOSED"
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        if (session.isNew() || session.getAttribute("user") == null) {

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().println("unauthorized user");
            return;
        }

        // Get parameters
        String type = null;

        try{

            type = StringEscapeUtils.escapeJava(request.getParameter("type"));
        } catch (NullPointerException e){

            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Incorrect or missing param values");
            return;
        }

        User user = (User) session.getAttribute("user");
        DashboardAuctionDAO dashboardAuctionDAO = new DashboardAuctionDAO(connection);
        List<DashboardAuction> auctionList = null;

        switch (type){

            case "OPEN":

                try{

                    auctionList = dashboardAuctionDAO.getListOfOpenAuctionsByUser(user.getId());
                } catch (SQLException e){

                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().println("Unable to recover Auction's list");
                    return;
                }
                break;

            case "CLOSED":

                try{

                    auctionList = dashboardAuctionDAO.getListOfClosedAuctionsByUser(user.getId());
                } catch (SQLException e){

                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().println("Unable to recover Auction's list");
                    return;
                }
                break;

            default:
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Incorrect type value");
                return;
        }

        // return a json of the list
        String json = new Gson().toJson(auctionList);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }
}
