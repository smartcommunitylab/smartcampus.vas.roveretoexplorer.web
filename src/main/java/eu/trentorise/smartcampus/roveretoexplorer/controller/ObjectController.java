/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.roveretoexplorer.controller;

import java.util.ArrayList;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.model.BaseDTObject;
import eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.model.CommunityData;
import eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.model.ExplorerObject;
import eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.model.Rating;
import eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.model.Review;
import eu.iescities.pilot.rovereto.roveretoexplorer.custom.data.model.ReviewObject;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.presentation.storage.sync.BasicObjectSyncStorage;
import eu.trentorise.smartcampus.profileservice.model.BasicProfile;
import eu.trentorise.smartcampus.storage.ReviewsMongoStorage;

@Controller
public class ObjectController extends AbstractObjectController {

	@Autowired
	private BasicObjectSyncStorage syncStorage;

	@Autowired
	private ReviewsMongoStorage reviewStorage;

	@RequestMapping(value = "/social/rate/{id}", method = RequestMethod.PUT)
	public void rate(HttpServletResponse response, @RequestParam String rating, @PathVariable String id) {
		String userId = null;
		try {
			try {
				userId = getUserId();
			} catch (SecurityException e) {
				logger.error("Failed to rate object with id " + id + " as user " + userId + ": " + e.getMessage());
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}

			BaseDTObject obj = (BaseDTObject) syncStorage.getObjectById(id);

			Integer iRating = Integer.parseInt(rating);
			updateRating(userId, obj, iRating);

			ReviewObject reviews = reviewStorage.getObjectById(id);

			if (reviews == null) {
				reviews = new ReviewObject(id);
			}

			int pos = -1, i = 0;

			for (Review oldReview : reviews.getReviews()) {
				if (userId.equals(oldReview.getUserId())) {
					pos = i;
					break;
				}
				i++;
			}
			if (pos < 0) {
				reviews.getReviews().add(new Review(userId, "", iRating));
			} else {
				Review r = reviews.getReviews().get(pos);
				r.setRating(iRating);
				reviews.getReviews().set(pos, r);
			}
			reviewStorage.storeObject(reviews);			
			syncStorage.storeObject(obj);
		} catch (NotFoundException e) {
			logger.error("Failed to rate object with id " + id + " as user " + userId + ": " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} catch (Exception e) {
			logger.error("Failed to rate object with id " + id + " as user " + userId + ": " + e.getMessage());
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

	}

	private Integer updateRating(String userId, BaseDTObject obj, Integer iRating) {
		if (iRating == null || iRating < 0)
			iRating = 0;
		if (iRating > 5)
			iRating = 5;

		double avg = 0;

		Rating newRating = null;

		if (obj.getCommunityData() == null) {
			CommunityData cd = new CommunityData();
			cd.setRatings(new ArrayList<Rating>());
			obj.setCommunityData(cd);
		}

		for (Rating rat : obj.getCommunityData().getRatings()) {
			if (userId.equals(rat.getUserId())) {
				newRating = rat;
				break;
			}
		}

		if (newRating == null) {
			newRating = new Rating(userId, iRating);
			obj.getCommunityData().getRatings().add(newRating);
		} else {
			newRating.setValue(iRating);
		}

		for (Rating rat : obj.getCommunityData().getRatings()) {
			avg += rat.getValue();
		}
		avg = avg / obj.getCommunityData().getRatings().size();

		obj.getCommunityData().setAverageRating((int) avg);
		obj.getCommunityData().setRatingsCount(obj.getCommunityData().getRatings().size());

		return iRating;
	}

	@RequestMapping(value = "/social/readReviews/{parentId}", method = RequestMethod.GET)
	public @ResponseBody
	ReviewObject getReviews(HttpServletRequest request, HttpServletResponse response, @PathVariable String parentId) {
		try {
			ReviewObject reviews = reviewStorage.getObjectById(parentId);

			if (reviews == null) {
				reviews = new ReviewObject(parentId);
			}

			return reviews;
		} catch (NotFoundException e) {
			logger.error("Failed to retrieve reviews for object with id " + parentId + ": " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		} catch (Exception e) {
			logger.error("Failed to retrieve reviews for object with id " + parentId + ": " + e.getMessage());
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}
	}

	@RequestMapping(value = "/social/review/{parentId}", method = RequestMethod.POST)
	public @ResponseBody
	CommunityData review(HttpServletRequest request, HttpServletResponse response, @RequestBody Review review, @PathVariable String parentId) {
		String userId = null;
		BasicProfile bp = null;
		try {
			try {
				bp = getUser();
				userId = bp.getUserId();
			} catch (SecurityException e) {
				logger.error("Failed to review object with id " + parentId + " as user " + userId + ": " + e.getMessage());
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return null;
			}

			ExplorerObject obj = (ExplorerObject) syncStorage.getObjectById(parentId);
			Integer rating = updateRating(userId, obj, review.getRating());
			review.setRating(rating);
			review.setUserId(userId);
			review.setAuthor(createAuthor(bp));
			review.setDate(System.currentTimeMillis());

			ReviewObject reviews = reviewStorage.getObjectById(parentId);

			if (reviews == null) {
				reviews = new ReviewObject(parentId);
			}

			int pos = -1, i = 0;

			for (Review oldReview : reviews.getReviews()) {
				if (userId.equals(oldReview.getUserId())) {
					pos = i;
					break;
				}
				i++;
			}
			if (pos < 0) {
				reviews.getReviews().add(review);
			} else {
				reviews.getReviews().set(pos, review);
			}
			reviewStorage.storeObject(reviews);
			syncStorage.storeObject(obj);
			obj.filterUserData(userId);
			return obj.getCommunityData();

		} catch (NotFoundException e) {
			logger.error("Failed to review object with id " + parentId + " as user " + userId + ": " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} catch (Exception e) {
			logger.error("Failed to review object with id " + parentId + " as user " + userId + ": " + e.getMessage());
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return null;
	}

	@RequestMapping(value = "/social/attend/{id}/{attend}", method = RequestMethod.POST)
	public void attend(HttpServletResponse response, @PathVariable String id, @PathVariable boolean attend) {
		String userId = null;
		try {
			try {
				userId = getUserId();
			} catch (SecurityException e) {
				logger.error("Failed to attend object with id " + id + " as user " + userId + ": " + e.getMessage());
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}

			BaseDTObject obj = (BaseDTObject) syncStorage.getObjectById(id);

			Set<String> attending = obj.getCommunityData().getAttending();
			if (attend) {
				attending.add(userId);
			} else {
				attending.remove(userId);
			}
			obj.getCommunityData().setAttendees(attending.size());

			syncStorage.storeObject(obj);
		} catch (NotFoundException e) {
			logger.error("Failed to rate object with id " + id + " as user " + userId + ": " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} catch (Exception e) {
			logger.error("Failed to rate object with id " + id + " as user " + userId + ": " + e.getMessage());
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

	}	
	
	@RequestMapping(value = "/social/edit", method = RequestMethod.POST)
	public @ResponseBody
	CommunityData edit(HttpServletRequest request, HttpServletResponse response, @RequestBody ExplorerObject newObject) {
		String userId = null;
		BasicProfile bp = null;
		try {
			try {
				bp = getUser();
				userId = bp.getUserId();
			} catch (SecurityException e) {
				logger.error("Failed to edit object: " + e.getMessage());
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return null;
			}
			
			ExplorerObject oldObject = (ExplorerObject)syncStorage.getObjectById(newObject.getId());
			if (oldObject == null) {
				logger.error("Trying to edit a non-existent object");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return null;
			}
			
//			newObject.setCommunityData(oldObject.getCommunityData());

			// TODO: enable
			if (oldObject.getVersion() > newObject.getVersion()) {
				logger.error("Trying to edit a more recent object version");
				response.setStatus(HttpServletResponse.SC_CONFLICT);
				return null;
			}

			syncStorage.storeObject(newObject);

		} catch (Exception e) {
			logger.error("Failed to edit object: " + e.getMessage());
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return null;
	}

	private String createAuthor(BasicProfile bp) {
		if (bp.getName() != null && bp.getSurname() != null)
			return bp.getName() + " " + bp.getSurname();
		if (bp.getName() != null)
			return bp.getName();
		if (bp.getSurname() != null)
			return bp.getSurname();
		return "";
	}

	// @RequestMapping(value = "/social/review/{parentId}", method =
	// RequestMethod.PUT)
	// public void updateReview(HttpServletRequest request, HttpServletResponse
	// response, @RequestBody Map<String, Object> pars, @PathVariable String
	// parentId) {
	// String userId = null;
	// try {
	// try {
	// userId = getUserId();
	// } catch (SecurityException e) {
	// logger.error("Failed to update review object with id " + parentId +
	// " as user " + userId + ": " + e.getMessage());
	// response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	// return;
	// }
	//
	// BaseDTObject obj = (BaseDTObject) syncStorage.getObjectById(parentId);
	//
	// if (obj instanceof InfoObject) {
	// logger.error("Cannot review InfoObject with id " + parentId + " as user " +
	// userId);
	// response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	// return;
	// }
	//
	// ReviewObject reviews = reviewStorage.getObjectById(parentId);
	//
	// String reviewString = (String) pars.get("review");
	// Review review = null;
	// for (Review oldReview : reviews.getReviews()) {
	// if (userId.equals(oldReview.getUserId())) {
	// review = oldReview;
	// break;
	// }
	// }
	//
	// if (review == null) {
	// response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	// return;
	// }
	//
	// review.setComment(reviewString);
	//
	// reviewStorage.storeObject(reviews);
	// } catch (NotFoundException e) {
	// logger.error("Failed to update review object with id " + parentId +
	// " as user " + userId + ": " + e.getMessage());
	// response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	// } catch (Exception e) {
	// logger.error("Failed to update review object with id " + parentId +
	// " as user " + userId + ": " + e.getMessage());
	// e.printStackTrace();
	// response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	// }
	//
	// }
	//
	// @RequestMapping(value = "/social/review/{parentId}", method =
	// RequestMethod.DELETE)
	// public void deleteReview(HttpServletRequest request, HttpServletResponse
	// response, @PathVariable String parentId) {
	// String userId = null;
	// try {
	// try {
	// userId = getUserId();
	// } catch (SecurityException e) {
	// logger.error("Failed to delete review object with id " + parentId +
	// " as user " + userId + ": " + e.getMessage());
	// response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	// return;
	// }
	//
	// BaseDTObject obj = (BaseDTObject) syncStorage.getObjectById(parentId);
	//
	// if (obj instanceof InfoObject) {
	// logger.error("Cannot delete review for InfoObject with id " + parentId +
	// " as user " + userId);
	// response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	// return;
	// }
	//
	// ReviewObject reviews = reviewStorage.getObjectById(parentId);
	//
	// Review review = null;
	// for (Review oldReview : reviews.getReviews()) {
	// if (userId.equals(oldReview.getUserId())) {
	// review = oldReview;
	// break;
	// }
	// }
	//
	// if (review != null) {
	// reviews.getReviews().remove(review);
	// reviewStorage.storeObject(reviews);
	// response.setStatus(HttpStatus.OK.value());
	// } else {
	// response.setStatus(HttpStatus.METHOD_FAILURE.value());
	// }
	//
	// } catch (NotFoundException e) {
	// logger.error("Failed to delete review object with id " + parentId +
	// " as user " + userId + ": " + e.getMessage());
	// response.setStatus(HttpStatus.METHOD_FAILURE.value());
	// } catch (Exception e) {
	// logger.error("Failed to delete review object with id " + parentId +
	// " as user " + userId + ": " + e.getMessage());
	// e.printStackTrace();
	// response.setStatus(HttpStatus.METHOD_FAILURE.value());
	// }
	// }
	//
}
