package raven.goals;

import raven.game.RavenBot;
import raven.game.messaging.Telegram;
import raven.math.Vector2D;

public class Goal_Explore extends GoalComposite<RavenBot> {
	  
	  Vector2D  m_CurrentDestination;
	  
	  
	  private boolean m_bDestinationIsSet;
	
	
	
	
	public Goal_Explore(RavenBot m_pOwner) {
		setM_pOwner(m_pOwner);
		this.m_bDestinationIsSet = false;
	}
	
	
	
	

	 public void activate(){
		 m_iStatus = Goal.curStatus.active;

		  //if this goal is reactivated then there may be some existing subgoals that
		  //must be removed
		  removeAllSubgoals();

		  if (!m_bDestinationIsSet)
		  {
		    //grab a random location
		    m_CurrentDestination = getM_pOwner().getWorld().getMap().getRandomNodeLocation();

		    m_bDestinationIsSet = true;
		  }

		  //and request a path to that position
		  getM_pOwner().getPathPlanner().RequestPathToPosition(m_CurrentDestination);

		  //the bot may have to wait a few update cycles before a path is calculated
		  //so for appearances sake it simple ARRIVES at the destination until a path
		  //has been found
		  AddSubgoal(new Goal_SeekToPosition(getM_pOwner(), m_CurrentDestination));
		 
		 
		 
	}

	  public raven.goals.Goal.curStatus process(){  //if status is inactive, call Activate()
		  activateIfInactive();

		  //process the subgoals
		  m_iStatus = ProcessSubgoals();

		  return m_iStatus;
	}

	  public void terminate(){}

	  public boolean HandleMessage(Telegram msg){
		  //first, pass the message down the goal hierarchy
		  boolean bHandled = ForwardMessageToFrontMostSubgoal(msg);

		  //if the msg was not handled, test to see if this goal can handle it
		  if (bHandled == false)
		  {
		    switch(msg.msg)
		    {
		    case MSG_PATH_READY:

		      //clear any existing goals
		      removeAllSubgoals();

		      AddSubgoal(new Goal_FollowPath(getM_pOwner(),
		                                     getM_pOwner().getPathPlanner().getPath()));

		      return true; //msg handled


		    case MSG_NO_PATH_AVAILABLE:

		      m_iStatus = Goal.curStatus.failed;

		      return true; //msg handled

		    default: return false;
		    }
		  }

		  //handled by subgoals
		  return true;
	  }

	@Override
	public void renderAtPos(Vector2D p) {
		// do nothing
		
	}

	@Override
	public void render() {
		// do nothing
		
	}

}
