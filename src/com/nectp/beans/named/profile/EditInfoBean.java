package com.nectp.beans.named.profile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import com.nectp.beans.named.upload.FileUploadImpl;
import com.nectp.beans.remote.daos.EmailFactory;
import com.nectp.beans.remote.daos.PlayerForSeasonService;
import com.nectp.beans.remote.daos.PlayerService;
import com.nectp.jpa.entities.Player;
import com.nectp.jpa.entities.PlayerForSeason;

@Named(value="editInfoBean")
@ViewScoped
public class EditInfoBean extends FileUploadImpl {
	private static final long serialVersionUID = 5954443552970720129L;

	private String name;
	private String nickname;
	private String email;
	
	private String oldPassword;
	private String newPassword;
	private String confirmPassword;
	
	private Logger log;
	
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
	
	@Override
	public void upload(FileUploadEvent event) {
		files.add(event.getFile());
	}
	
	public void saveChanges(ActionEvent event) {
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
			pfsService.update(profileEntity);
		}
		
		if (email != null && !email.isEmpty()) {
			emailFactory.createEmailForPlayer(player, email, true, true);
		}
		
		if (oldPassword != null && newPassword != null && confirmPassword != null) {
			if (oldPassword.equals(player.getPassword()) && 
				newPassword.equals(confirmPassword) && 
				!newPassword.equals(oldPassword)) {
				player.setPassword(newPassword);
				updatePlayer = true;
				log.info("new password set to: " + newPassword);
			}
		}
		
 		//	If a file was uploaded, save it to the server & set the avatar url
		if (files.size() == 1) {
			File destFile = null;
			UploadedFile uploaded = files.get(0);
			try {
				String path = System.getProperty("user.home") + File.separator + "NECTrackerResources" + 
						File.separator + "Avatars" + File.separator;
				destFile = new File(path + uploaded.getFileName());
				InputStream stream = uploaded.getInputstream();
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
				player.setAvatarUrl(destFile.getAbsolutePath());
				updatePlayer = true;
			}
		}
		
		if (updatePlayer) {
			log.info("updating player:");
			playerService.update(player);
		}
	}

	@Override
	public void submit() {
		
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
