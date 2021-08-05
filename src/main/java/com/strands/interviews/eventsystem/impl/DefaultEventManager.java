package com.strands.interviews.eventsystem.impl;

import com.strands.interviews.eventsystem.EventManager;
import com.strands.interviews.eventsystem.InterviewEvent;
import com.strands.interviews.eventsystem.InterviewEventListener;

import java.util.*;

/**
 * Manages the firing and receiving of events.
 *
 * <p>Any event passed to {@link #publishEvent} will be passed through to "interested" listeners.
 *
 * <p>Event listeners can register to receive events via
 * {@link #registerListener(String, com.strands.interviews.eventsystem.InterviewEventListener)}
 */
public class DefaultEventManager implements EventManager
{
    private Map listeners = new HashMap();
    private Map listenersByClass = new HashMap();
    private Map listenerSpecial = new HashMap();

    public void publishEvent(InterviewEvent event)
    {
        if (event == null)
        {
            System.err.println("Null event fired?");
            return;
        }

        sendEventTo(event, calculateListeners(event.getClass()));
    }

    public void publishEvent(InterviewEvent event, Boolean isTask3) {
        if (event == null) {
            System.err.println("Null event fired?");
            return;
        }

        if(isTask3)
            sendEventTo(event, calculateListeners(event.getClass(), !event.getClass().getSuperclass().equals(Object.class)));
        else
            sendEventTo(event, calculateListeners(event.getClass()));
    }

    private Collection calculateListeners(Class eventClass, boolean isChild)
    {
        List totalListener = (List) calculateListeners(eventClass);

        if(isChild)
            totalListener.addAll((List) listenersByClass.get(eventClass.getSuperclass()));

        return totalListener;
    }

    private Collection calculateListeners(Class eventClass)
    {
        List totalListener = listenersByClass.get(eventClass) != null ? (List) listenersByClass.get(eventClass) : new ArrayList();

        if(listenerSpecial.values().size() > 0) totalListener.addAll(listenerSpecial.values());

        return totalListener;
    }

    public void registerListener(String listenerKey, InterviewEventListener listener)
    {
        if (listenerKey == null || listenerKey.equals(""))
            throw new IllegalArgumentException("Key for the listener must not be null: " + listenerKey);

        if (listener == null)
            throw new IllegalArgumentException("The listener must not be null: " + listener);

        if (listeners.containsKey(listenerKey))
            unregisterListener(listenerKey);

        Class[] classes = listener.getHandledEventClasses();

        for (int i = 0; i < classes.length; i++)
            addToListenerList(classes[i], listener);

        listeners.put(listenerKey, listener);

        if(classes.length == 0)
            listenerSpecial.put(listenerKey, listener);
    }

    public void unregisterListener(String listenerKey)
    {
        InterviewEventListener listener = (InterviewEventListener) listeners.get(listenerKey);

        for (Iterator it = listenersByClass.values().iterator(); it.hasNext();)
        {
            List list = (List) it.next();
            list.remove(listener);
        }

        listeners.remove(listenerKey);
    }

    private void sendEventTo(InterviewEvent event, Collection listeners)
    {
        if (listeners == null || listeners.size() == 0)
            return;

        for (Iterator it = listeners.iterator(); it.hasNext();)
        {
            InterviewEventListener eventListener = (InterviewEventListener) it.next();
            eventListener.handleEvent(event);
        }
    }

    private void addToListenerList(Class aClass, InterviewEventListener listener)
    {
        if (!listenersByClass.containsKey(aClass))
            listenersByClass.put(aClass, new ArrayList());

        ((List)listenersByClass.get(aClass)).add(listener);
    }

    public Map getListeners()
    {
        return listeners;
    }
}
