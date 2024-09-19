/*--* 
 * =========================================================================== 
 * � 2019
 * Fidelity National Information Services, Inc. and/or its subsidiaries - All 
 * Rights Reserved worldwide.
 * --------------------------------------------------------------------------- 
 * This document is protected under the trade secret and copyright laws as the 
 * property of Fidelity National Information Services, Inc. and/or its 
 * subsidiaries.
 * 
 * Copying, reproduction or distribution should be limited and only to 
 * employees with a "need to know" to do their job. Any disclosure of this 
 * document to third parties is strictly prohibited.
 *  Programmer: Deepak Mishra
 * ============================================================================ 
*  M A I N T E N A N C E   L O G
* 
* 	PGMR	    DATE			    		      Changes
*   DEEPAKM		05/15/19				        Initial Coding.
 
*/
package com.etherealyt.springbootapp.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public abstract class DBConnectionUtility {
	public static Connection getConnection(String urlString, String username, String password)
			throws SQLException, ClassNotFoundException {
		Connection con = null;

		// Register the PostgreSQL driver
		Class.forName("org.postgresql.Driver");

		try {
			// Attempt to establish a connection to the PostgreSQL database
			con = DriverManager.getConnection(urlString, username, password);
		} catch (SQLException ex) {
			System.out.println("Failed to create the database connection.");
			ex.printStackTrace();
		}

		return con;
	}


}
