// =================================================================                                                                   
// Copyright (C) 2011-2015 Pierre Lison (plison@ifi.uio.no)                                                                            
//                                                                                                                                     
// This library is free software; you can redistribute it and/or                                                                       
// modify it under the terms of the GNU Lesser General Public License                                                                  
// as published by the Free Software Foundation; either version 2.1 of                                                                 
// the License, or (at your option) any later version.                                                                                 
//                                                                                                                                     
// This library is distributed in the hope that it will be useful, but                                                                 
// WITHOUT ANY WARRANTY; without even the implied warranty of                                                                          
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU                                                                    
// Lesser General Public License for more details.                                                                                     
//                                                                                                                                     
// You should have received a copy of the GNU Lesser General Public                                                                    
// License along with this program; if not, write to the Free Software                                                                 
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA                                                                           
// 02111-1307, USA.                                                                                                                    
// =================================================================                                                                   

package opendial.arch;


import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import opendial.modules.Module;
import opendial.readers.XMLSettingsReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * System-wide settings for openDial.
 *
 * @author  Pierre Lison (plison@ifi.uio.no)
 * @version $Date::                      $
 *
 */
public class Settings {

	// logger
	public static Logger log = new Logger("Settings", Logger.Level.DEBUG);
	
	/** Default settings */
	public static final String SETTINGS_FILE = "resources//settings.xml";

	/** maximum number of samples to use for likelihood weighting */
	public static int nbSamples = 1000;
	
	/** maximum sampling time (in milliseconds) */
	public static long maxSamplingTime = 250 ;
	 
	/** Number of discretisation buckets to convert continuous distributions */
	public static int discretisationBuckets = 50;

	/** Whether to show the GUI */
	public boolean showGUI;

	/** Variable label for the user input */
	public String userInput;
	
	/** Variable label for the system output */
	public String systemOutput;
		
	/** Other variables to monitor in the chat window */
	public List<String> varsToMonitor = new ArrayList<String>();

	/** Planning horizon */
	public int horizon;

	/** Discount factor for forward planning */
	public double discountFactor;
	
	/** Recording types */
	public static enum Recording {NONE, LAST_INPUT, ALL}
	
	/** Whether to record intermediate dialogue state */
	public Recording recording = Recording.LAST_INPUT;
	
	/** Other parameters */
	public Properties params = new Properties();

	/** Domain-specific modules to run */
	public List<Class<Module>> modules = new ArrayList<Class<Module>>();
	
	
	/**
	 * Creates new settings with the default values
	 */
	public Settings() {
		fillSettings(XMLSettingsReader.extractMapping(SETTINGS_FILE));
	}
	
	/**
	 * Creates a new settings with the values provided as argument.  Values
	 * that are not explicitly specified in the mapping are set to their
	 * default values.
	 * 
	 * @param mapping the properties
	 */
	public Settings(Properties mapping) {
		fillSettings(XMLSettingsReader.extractMapping(SETTINGS_FILE));
		fillSettings(mapping);	
	}
		
	
	/**
	 * Fills the current settings with the values provided as argument. 
	 * Existing values are overridden.
	 * 
	 * @param mapping the properties
	 */
	@SuppressWarnings("unchecked")
	public void fillSettings(Properties mapping) {

		for (String key : mapping.stringPropertyNames()) {
			if (key.equalsIgnoreCase("horizon")) {
				horizon = Integer.parseInt(mapping.getProperty(key));
			}
			else if (key.equalsIgnoreCase("discount")) {
				discountFactor = Double.parseDouble(mapping.getProperty(key));
			}

			else if (key.equalsIgnoreCase("gui")) {
				showGUI = Boolean.parseBoolean(mapping.getProperty(key));
			}
			else if (key.equalsIgnoreCase("user")) {
				userInput = mapping.getProperty(key);
			}
			else if (key.equalsIgnoreCase("system")) {
				systemOutput = mapping.getProperty(key);
			}
			else if (key.equalsIgnoreCase("monitor")) {
				String[] split = mapping.getProperty(key).split(",");
				for (int i = 0 ; i < split.length ; i++) {
					if (split[i].trim().length() > 0) {
					varsToMonitor.add(split[i].trim());
					}
				}
			}
			else if (key.equalsIgnoreCase("samples")) {
				nbSamples = Integer.parseInt(mapping.getProperty(key));
			}
			else if (key.equalsIgnoreCase("timeout")) {
				maxSamplingTime = Integer.parseInt(mapping.getProperty(key));
			}
			else if (key.equalsIgnoreCase("discretisation")) {
				discretisationBuckets = Integer.parseInt(mapping.getProperty(key));
			}
			else if (key.equalsIgnoreCase("recording")) {
				if (mapping.getProperty(key).trim().equalsIgnoreCase("last") ) {
					recording = Recording.LAST_INPUT;
				}
				else if (mapping.getProperty(key).trim().equalsIgnoreCase("all")) {
					recording = Recording.ALL;
				}
				else {
					recording = Recording.NONE;
				}
			}
			else if (key.equalsIgnoreCase("modules")) {
				String[] split = mapping.getProperty(key).split(",");
				for (int i = 0 ; i < split.length ; i++) {
					if (split[i].trim().length() > 0) {
						Class<?> clazz;
						try {
							clazz = Class.forName(split[i].trim());
							for (int j = 0 ; j < clazz.getInterfaces().length ; j++) {
								if (clazz.getInterfaces()[j].equals(Module.class) && !modules.contains(clazz)) {
									modules.add((Class<Module>)clazz);
								}
							}
						} catch (ClassNotFoundException e) {
							log.warning("class not found: " + split[i].trim());
						}
					}
				}
			}
			else {
				params.put(key, mapping.getProperty(key));
			}
		}
	}
	
	
	/**
	 * Returns a representation of the settings in terms of a mapping
	 * between property labels and values
	 * 
	 * @return the corresponding mapping
	 */
	public Properties getFullMapping() {
		Properties mapping = new Properties();
		mapping.putAll(params);
		mapping.setProperty("horizon", ""+horizon);
		mapping.setProperty("discount", ""+discountFactor);
		mapping.setProperty("gui", ""+showGUI);
		mapping.setProperty("user", ""+userInput);
		mapping.setProperty("system", ""+systemOutput);
		mapping.setProperty("monitor", varsToMonitor.toString().replace("[", "").replace("]", ""));
		mapping.setProperty("samples", ""+nbSamples);
		mapping.setProperty("timeout", ""+maxSamplingTime);
		mapping.setProperty("discretisation", ""+discretisationBuckets);
		List<String> moduleNames = new ArrayList<String>();
		for (Class<Module> m : modules) { moduleNames.add(m.getCanonicalName()); }
		mapping.setProperty("modules", ""+moduleNames.toString().replace("[", "").replace("]", ""));
		return mapping;
	}


	/**
	 * Generates an XML element that encodes the settings
	 * 
	 * @param doc the document to which the element must comply
	 * @return the resulting XML element
	 * @throws DialException if the XML generation failed
	 */
	public Element generateXML(Document doc) throws DialException {

		Element root = doc.createElement("settings");
		
		Properties mapping = getFullMapping();
		for (String otherParam : mapping.stringPropertyNames()) {
			Element otherParamE = doc.createElement(otherParam);
			otherParamE.setTextContent(""+mapping.get(otherParam));
			root.appendChild(otherParamE);
		}
			
		return root;
	}
	
	
	/**
	 * Copies the settings
	 * 
	 * @return the copy
	 */
	public Settings copy() {
		return new Settings(getFullMapping());
	}
	
	
	/**
	 * Returns a string representation of the settings
	 * 
	 * @return the settings
	 */
	@Override
	public String toString() {
		return getFullMapping().toString();
	}

	
	
}
