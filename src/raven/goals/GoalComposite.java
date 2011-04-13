package raven.goals;

import java.util.ArrayList;

import raven.game.BaseGameEntity;
import raven.game.RavenBot;
import raven.game.messaging.Telegram;
import raven.math.Vector2D;

abstract public class GoalComposite<T extends BaseGameEntity> extends Goal<T> {

	  public ArrayList <Goal<T> > m_SubGoals;
	  private RavenBot m_pOwner;
	
	public raven.goals.Goal.curStatus ProcessSubgoals(){ 
	  //remove all completed and failed goals from the front of the subgoal list
	  while (!m_SubGoals.isEmpty() &&
	         (m_SubGoals.get(0).isComplete() || m_SubGoals.get(0).hasFailed()))
	  {    
	    m_SubGoals.get(0).Terminate();
	    m_SubGoals.remove(0);
	  }

	  //if any subgoals remain, process the one at the front of the list
	  if (!m_SubGoals.isEmpty())
	  { 
	    //grab the status of the front-most subgoal
	    raven.goals.Goal.curStatus StatusOfSubGoals = m_SubGoals.get(0).Process();

	    //we have to test for the special case where the front-most subgoal
	    //reports 'completed' *and* the subgoal list contains additional goals.When
	    //this is the case, to ensure the parent keeps processing its subgoal list
	    //we must return the 'active' status.
	    if (StatusOfSubGoals == Goal.curStatus.completed && m_SubGoals.size() > 1)
	    {
	      return Goal.curStatus.active;
	    }

	    return StatusOfSubGoals;
	  }
	  
	  //no more subgoals to process - return 'completed'
	  else
	  {
	    return Goal.curStatus.completed;
	  }
	}
	
	public void removeAllSubgoals() {
		m_SubGoals = new ArrayList<Goal <T>>();
	}
	
	public void AddSubgoal(Goal<T> g)
	{   
	  //add the new goal to the front of the list
	  m_SubGoals.add(g);
	}

	
    public boolean ForwardMessageToFrontMostSubgoal(Telegram msg)
    {
      if (!m_SubGoals.isEmpty())
      {
        return m_SubGoals.get(0).HandleMessage(msg);
      }

      //return false if the message has not been handled
      return false;
    }
    
	abstract public void renderAtPos(Vector2D p);
	
	abstract public void render();

	public void setM_pOwner(RavenBot m_pOwner) {
		this.m_pOwner = m_pOwner;
	}

	public RavenBot getM_pOwner() {
		return m_pOwner;
	}
	
	
}
