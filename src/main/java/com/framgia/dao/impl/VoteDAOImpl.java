package com.framgia.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.criterion.Restrictions;

import com.framgia.dao.AbstractDAO;
import com.framgia.dao.VoteDAO;
import com.framgia.model.Vote;

public class VoteDAOImpl extends AbstractDAO<Integer, Vote> implements VoteDAO {

	@Override
	public void create(Vote vote) {
		logger.info("VoteDAO _ create vote");
		saveOrUpdate(vote);
	}
	@Override
	public void deleteVote(Vote vote) {
		logger.info("VoteDAO _ delete vote");
		delete(vote);
	}

	@Override
	public Vote findVoteToDelete(Integer idImage, Integer idUser) {
		logger.info("UserDAO _ findByUsername");
		Criteria crit = getSession().createCriteria(Vote.class);
		crit.add(Restrictions.eq("image.id", idImage));
		crit.add(Restrictions.eq("user.id", idUser));
		crit.setLockMode(LockMode.UPGRADE);

		return (Vote) crit.uniqueResult();
	}

}