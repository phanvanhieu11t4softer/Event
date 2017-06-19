package com.framgia.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.framgia.bean.UserInfo;
import com.framgia.model.Group;
import com.framgia.model.Image;
import com.framgia.model.User;
import com.framgia.security.CustomUserDetail;
import com.framgia.service.UserService;
import com.framgia.util.Constants;
import com.framgia.util.ConvetBeanAndModel;
import com.framgia.util.DateUtil;
import com.framgia.util.Helpers;

/**
 * 
 * @version 22/05/2017
 * @author vu.thi.tran.van@framgia.com
 * 
 */
@SuppressWarnings("serial")
public class UserServiceImpl extends BaseServiceImpl implements UserService {

	private static final Logger logger = Logger.getLogger(UserServiceImpl.class);

	@Override
	public UserInfo findById(Integer id, boolean flagUpdate) {
		try {
			return ConvetBeanAndModel.convertUserModelToBean(getUserDAO().findById(id, flagUpdate));
		} catch (Exception e) {
			logger.error("Group service _ findById", e);
			return null;
		}
	}

	@Override
	public CustomUserDetail findByUserName(String username) {
		try {
			User user = getUserDAO().findByUserName(username);
			if (user != null) {
				List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();
				CustomUserDetail customUser = new CustomUserDetail();
				customUser.setUserId(user.getId().toString());
				customUser.setUsername(user.getUsername());
				customUser.setPassword(user.getPassword());
				if (user.getPermission() != null) {
					authList.add(new SimpleGrantedAuthority(user.getPermission().getName()));
					customUser.setAuthorities(authList);
				}

				return customUser;
			}
		} catch (Exception e) {
			logger.error("findByUserName", e);
		}
		return null;
	}

	@Override
	public boolean addUser(UserInfo userInfo) {
		try {
			if (StringUtils.isBlank(Helpers.getUsername())) {

				// Guest register
				userInfo.setUserCreate(userInfo.getUsername());
				userInfo.setUserUpdate(userInfo.getUsername());
			} else {

				// Manger of register
				userInfo.setUserCreate(Helpers.getUsername());
				userInfo.setUserUpdate(Helpers.getUsername());
			}
			userInfo.setDateCreate(DateUtil.getDateNow());
			userInfo.setDateUpdate(DateUtil.getDateNow());
			userInfo.setDeleteFlag(Constants.DEL_FLG);
			User user = ConvetBeanAndModel.convertUserBeanToModel(userInfo);
			userDAO.saveOrUpdate(user);
			return true;
		} catch (Exception e) {
			logger.error("addUser", e);
		}
		return false;
	}

	@Override
	public boolean isUserExist(UserInfo userInfo) {
		try {
			User user = getUserDAO().findByUserName(userInfo.getUsername());
			if (user != null)
				return true;
		} catch (Exception e) {
			logger.error("isUserExist", e);
		}
		return false;
	}

	@Override
	public boolean updatetUser(UserInfo userInfo) {
		try {
			User user = ConvetBeanAndModel.convertUserBeanToModel(userInfo);
			userDAO.saveOrUpdate(user);
			return true;
		} catch (Exception e) {
			logger.error("update user", e);
			throw e;
		}

	}

	@Override
	public boolean removeUser(Integer id, Integer idGroup) {

		try {
			User user = getUserDAO().findById(id, true);
			if (!user.getIdGroup().equals(idGroup))
				return false;

			// update
			user.setIdGroup(null);
			user.setStatusJoin(Constants.STATUSJOIN_CODE_FREE);
			user.setDateUpdate(DateUtil.getDateNow());
			user.setUserUpdate(Helpers.getUsername());

			userDAO.saveOrUpdate(user);

			// delete image
			Image image = imageDAO.getImageByUserCreate(user.getUsername(), idGroup);
			if (image != null) {
				imageDAO.delete(image);
			}

			return true;
		} catch (Exception e) {
			logger.error("remove user", e);
			throw e;
		}
	}

	@Override
	public boolean acceptUserJoinGroup(Integer id, Integer idGroup) {

		try {
			User user = getUserDAO().findById(id, true);
			if (!user.getIdGroup().equals(idGroup))
				return false;

			// update
			user.setStatusJoin(Constants.STATUSJOIN_CODE_APPOVE);
			user.setDateUpdate(DateUtil.getDateNow());
			user.setUserUpdate(Helpers.getUsername());

			userDAO.saveOrUpdate(user);
			return true;
		} catch (Exception e) {
			logger.error("remove user", e);
			throw e;
		}
	}

	@Override
	public boolean requestUserJoinGroup(Integer idGroup, Integer idUser) {
		try {
			Group group = groupDAO.findById(idGroup, false);
			if (group == null) {
				return false;
			} else if (Constants.GROUP_TYPE_CODE_PRIVATE.equals(group.getType())
			        || Constants.DEL_FLG_DEL.equals(group.getDeleteFlag())) {
				return false;
			}
			User user = userDAO.findById(idUser, true);

			user.setStatusJoin(Constants.STATUSJOIN_CODE_REQUEST);
			user.setIdGroup(idGroup);
			user.setDateUpdate(DateUtil.getDateNow());
			user.setUserUpdate(user.getUsername());

			userDAO.saveOrUpdate(user);
			return true;
		} catch (Exception e) {
			logger.error("user request to join this group", e);
			throw e;
		}
	}

	@Override
	public Integer getInfoUser(Integer idUser) {
		try {
			User user = userDAO.findById(idUser, false);
			if (user == null) {
				return null;
			}
			if (user.getPermission().getId() == 1) {
				return 1;
			} else if (user.getPermission().getId() == 2) {
				return 2;
			} else {
				if (Constants.STATUSJOIN_CODE_APPOVE.equals(user.getStatusJoin()) && user.getIdGroup() != null) {
					return 3;
				} else {
					return 4;
				}
			}
		} catch (Exception e) {
			logger.error("getInfoUser", e);
		}
		return null;
	}

	@Override
	public boolean leaveGroup(Integer idUser) {
		try {
			User user = userDAO.findById(idUser, true);
			if (user == null)
				return false;

			user.setIdGroup(null);
			user.setStatusJoin(Constants.STATUSJOIN_CODE_FREE);
			user.setDateUpdate(DateUtil.getDateNow());
			user.setUserUpdate(user.getUsername());
			return true;
		} catch (Exception e) {
			logger.error("leaveGroup error", e);
			throw e;
		}
	}
}