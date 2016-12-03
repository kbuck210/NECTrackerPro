package com.nectp.beans.named.profile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.model.UploadedFile;

import com.nectp.beans.remote.daos.EmailFactory;
import com.nectp.beans.remote.daos.PlayerForSeasonService;
import com.nectp.beans.remote.daos.PlayerService;
import com.nectp.jpa.entities.Player;
import com.nectp.jpa.entities.PlayerForSeason;

@Named(value="editInfoBean")
@ViewScoped
public class EditInfoBean implements Serializable {
	private static final long serialVersionUID = 5954443552970720129L;

	private String name;
	private String nickname;
	private String email;
	
	private String oldPassword;
	private String newPassword;
	private String confirmPassword;
	
	private Logger log;
	
	private UploadedFile file;
	
	@EJB
	private PlayerForSeasonService pfsService;
	
	@EJB
	private PlayerService playerService;
	
	@EJB
	private EmailFactory emailFactory;
	
	private PlayerForSeason profileEntity;
	
	public EditInfoBean() {
		log = Logger.getLogger(EditInfoBean.class.getName());
	}
	
	public UploadedFile getFile() {
		return file;
	}
	
	public void setFile(UploadedFile file) {
		log.info("setting file: " + file.getFileName());
		this.file = file;
	}
	
	public void submit(ActionEvent event) {
		log.info("submitting.");
		Player player = profileEntity.getPlayer();
		
		//	If the name was updated, update the player
		boolean updatePlayer = false;
		if (name != null && !name.isEmpty() && !player.getName().equals(name)) {
			player.setName(name);
			updatePlayer = true;
		}
		
		if (nickname != null && !nickname.isEmpty() && 
				!profileEntity.getNickname().equals(nickname)) {
			profileEntity.setNickname(nickname);
			boolean nickUpdate = pfsService.update(profileEntity);
			if (nickUpdate) {
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success!", "Nickname updated");
				FacesContext.getCurrentInstance().addMessage(null, message);
			}
			else {
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR:", "Failed to update nickname!");
				FacesContext.getCurrentInstance().addMessage(null, message);
			}
		}
		
		if (email != null && !email.isEmpty()) {
			emailFactory.createEmailForPlayer(player, email, true, true);
		}
		
		if (oldPassword != null && !oldPassword.isEmpty() && 
			newPassword != null && !newPassword.isEmpty() && 
			confirmPassword != null && !confirmPassword.isEmpty()) 
		{
			if (oldPassword.equals(player.getPassword()) && 
				newPassword.equals(confirmPassword) && 
				!newPassword.equals(oldPassword)) {
				player.setPassword(newPassword);
				updatePlayer = true;
				log.info("new password set to: " + newPassword);
			}
			else {
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR:",  "Invalid password/Passwords do not match!");
		        FacesContext.getCurrentInstance().addMessage(null, message);
			}
		}
		
 		//	If a file was uploaded, save it to the server & set the avatar url
		if (file != null) {
			File destFile = null;
			try {
//				String path = "/NECTrackerResources/avatars/" + file.getFileName();
				String path = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("upload.avatars") + file.getFileName();
				
				Path filePath = Paths.get(path);
				destFile = filePath.toFile();
				
				log.info("writing to file: " + destFile.getAbsolutePath());
				InputStream stream = file.getInputstream();
				try {
			        OutputStream out = new FileOutputStream(destFile);
			        byte[] buf = new byte[1024];
			        int length;
			        while((length=stream.read(buf))>0){
			            out.write(buf,0,length);
			        }
			        out.close();
			        stream.close();
			    } catch (Exception e) {
			        e.printStackTrace();
			    }
			} catch (IOException e) {
				log.severe("Exception caught copying file: " + e.getMessage());
				e.printStackTrace();
			}
			
			//	If the file was uploaded successfully, change the avatar url
			if (destFile != null && destFile.exists()) {
				player.setAvatarUrl("/avatars/" + file.getFileName());
				updatePlayer = true;
			}
			else {
				log.severe("File does not exist!");
			}
		}
		else {
			log.warning("No file uploaded - skipping file write logic.");
		}
		
		if (updatePlayer) {
			log.info("updating player:");
			boolean playerUpdated = playerService.update(player);
			if (playerUpdated) {
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success!", "Player updated successfully!");
				FacesContext.getCurrentInstance().addMessage(null, message);
			}
			else {
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR:", "Failed to update player.");
				FacesContext.getCurrentInstance().addMessage(null, message);
			}
		}
	}

	public void setProfileEntity(PlayerForSeason profileEntity) {
		this.profileEntity = profileEntity;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	public String getNickname() {
		return nickname;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}
	
	public String getOldPassword() {
		return oldPassword;
	}
	
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	
	public String getNewPassword() {
		return newPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}
	
	public String getConfirmPassword() {
		return confirmPassword;
	}
}
