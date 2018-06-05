/**
 *  Weewx Weather Driver
 *
 *  Copyright 2018 @Cobra
 *
 *  This driver was originally born from an idea by @mattw01 and @Jhoke and I thank them for that!
 *  
 *  This driver is specifically designed to be used with 'Weewx' and your own PWS
 *  It also has the capability to collect forecast data from Apixu.com (once you have an api key)
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Last Update 29/05/2018
 *
 *
 *  V1.0.0 - Original POC
 *
 */




metadata {
    definition (name: "Weewx Weather Driver - Beta1", namespace: "Cobra", author: "Andrew Parker") {
        capability "Actuator"
        capability "Sensor"
        capability "Temperature Measurement"
        capability "Illuminance Measurement"
        capability "Relative Humidity Measurement"
        command "PollStationNow"
		command "PollApixuNow"
        
// Base Info        
        attribute "DriverAuthor", "string"
        attribute "DriverVersion", "string"
        attribute "WeewxServerUptime", "string"
        attribute "WeewxServerLocation", "string"
        
// Units
        attribute "distanceUnit", "string"
        attribute "pressureUnit", "string"
        attribute "rainUnit", "string"
        attribute "summaryFormat", "string"
        
// Collected Local Station Data       
        attribute "solarradiation", "string"
        attribute "dewpoint", "string"
        attribute "inside_humidity", "string"
        attribute "inside_temperature", "string"
        attribute "pressure", "string"
        attribute "pressure_trend", "string"
        attribute "wind", "string"
        attribute "wind_gust", "string"
        attribute "wind_dir", "string"
        attribute "rain_rate", "string"
        attribute "uv", "string"
        attribute "uvHarm", "string"
        attribute "feelsLike", "string"
        attribute "LastUpdate-Weewx", "string"
        attribute "precip_1hr", "string"
        attribute "precip_today", "string"
        attribute "sunrise", "string"
        attribute "sunset", "string"
        attribute "moonPhase", "string"
        attribute "moonRise", "string"
        
        
        // Apixu Data (if used)
        attribute "LastUpdate-Apixu", "string"
        attribute "visibility", "string"
        attribute "forecastHigh", "string"
        attribute "forecastLow", "string"
        attribute "city", "string"
        attribute "state", "string"
        attribute "country", "string"
        attribute "weatherCurrent", "string"
        attribute "rainTomorrow", "string"
        attribute "rainDayAfterTomorrow", "string"
        attribute "weatherIcon", "string"
        attribute "weatherForecast", "string"
        
        
       
       
       
//       attribute "percentPrecip", "string"
//       attribute "wind_string", "string"
//       attribute "forecastConditions", "string"
//       attribute "weatherSummary", "string"
//       attribute "weatherSummaryFormat", "string"
 
        
        

 


     
        
    }
    preferences() {
        section("Query Inputs"){
            input "ipaddress", "text", required: true, title: "Weewx Server IP/URI"
            input "weewxPort", "text", required: true, title: "Connection Port", defaultValue: "80"
            input "weewxPath", "text", required: true, title: "path to file", defaultValue: "weewx/daily.json"
            input "unitSet", "bool", title: "Display Data Units", required: true, defaultValue: true
            input "logSet", "bool", title: "Log All Response Data", required: true, defaultValue: false
            input "pollInterval", "enum", title: "Weewx Station Poll Interval", required: true, defaultValue: "5 Minutes", options: ["Manual Poll Only", "5 Minutes", "10 Minutes", "15 Minutes", "30 Minutes", "1 Hour", "3 Hours"]
            input "pressureUnit", "enum", title: "Pressure Unit", required:true, defaultValue: "INHg", options: ["INHg", "MBAR"]
            input "rainUnit", "enum", title: "Rain Unit", required:true, defaultValue: "IN", options: ["IN", "MM"]
            input "speedUnit", "enum", title: "Wind Speed Unit", required:true, defaultValue: "MPH", options: ["MPH", "KPH"]
            input "temperatureUnit", "enum", title: "Temperature Unit", required:true, defaultValue: "Fahrenheit (�F)", options: ["Fahrenheit (�F)", "Celsius (�C)"]
            input "decimalUnit", "enum", title: "Max Decimal Places", required:true, defaultValue: "2", options: ["1", "2", "3", "4", "5"]
            input "addData", "bool", title: "Collect Additional Forecast Data From Apixu.com", required: true, defaultValue: false
            if (addData == true){
                input "apiKey", "text", required: true, title: "Apixu API Key"
                input "pollLocation1", "text", required: true, title: "ZIP Code or Location"
                input "iconType", "bool", title: "Apixu Icon: On = Current - Off = Forecast", required: true, defaultValue: false
                input "pollInterval1", "enum", title: "Apixu Poll Interval", required: true, defaultValue: "3 Hours", options: ["Manual Poll Only", "15 Minutes", "30 Minutes", "1 Hour", "3 Hours"]
            }
                
            
            
 
    //        input "cutOff", "time", title: "New Day Starts", required: true
    //        input "summaryType", "bool", title: "Full Weather Summary", required: true, defaultValue: false
    //        input "iconType", "bool", title: "Icon: On = Current - Off = Forecast", required: true, defaultValue: false
   //         input "weatherFormat", "enum", required: true, title: "How to format weather summary",  options: ["Celsius, Miles & MPH", "Fahrenheit, Miles & MPH", "Celsius, Kilometres & KPH"]
        }
    }
}

def updated() {
    log.debug "updated called"
    unschedule()
    units()
    PollStationNow()
    PollApixuNow()
    state.DisplayUnits = unitSet
    def pollIntervalCmd = (settings?.pollInterval ?: "3 Hours").replace(" ", "")
    
    if(pollInterval == "Manual Poll Only"){
        log.info "MANUAL POLLING ONLY"}
    else{
        "runEvery${pollIntervalCmd}"(pollSchedule)}
    
    def pollIntervalCmd1 = (settings?.pollInterval ?: "3 Hours").replace(" ", "")
    
    if(pollInterval1 == "Manual Poll Only"){
        log.info "MANUAL POLLING ONLY"}
    else{
        "runEvery${pollIntervalCmd1}"(pollSchedule1)}
    

}


def units(){
    state.SRU = " watts"
    state.IU = " watts"
 	state.HU = " %"   
    state.DecimalPlaces = decimalUnit.toInteger()
    state.DisplayUnits = unitSet
    
    
}





// Get APIXU data *******************************************************



def PollApixuNow(){
    units()
 log.debug "Apixu: Poll called"
    def params2 = [
          
          uri: "http://api.apixu.com/v1/forecast.json?key=${apiKey}&q=${pollLocation1}&days=3"
    ]
    

    try {
        httpGet(params2) { resp2 ->
            resp2.headers.each {
            log.debug "Response2: ${it.name} : ${it.value}"
        }
            if(logSet == true){  
           
            log.debug "params2: ${params2}"
            log.debug "response contentType: ${resp2.contentType}"
 		    log.debug "response data: ${resp2.data}"
            } 
            if(logSet == false){ 
            log.info "Further detailed Apixu data logging disabled"    
            }    
            
    
            
            // Apixu No Units ********************
   
            
              sendEvent(name: "weatherCurrent", value: resp2.data.current.condition.text, isStateChange: true)
              sendEvent(name: "weatherForecast", value: resp2.data.forecast.forecastday.day[1].condition.text, isStateChange: true)
              sendEvent(name: "city", value: resp2.data.location.name, isStateChange: true)
              sendEvent(name: "state", value: resp2.data.location.region, isStateChange: true)
              sendEvent(name: "country", value: resp2.data.location.country, isStateChange: true)
              sendEvent(name: "LastUpdate-Apixu", value: resp2.data.current.last_updated, isStateChange: true)    
              
             
                
                
            
            // Select Apixu Icon
                if(iconType == false){   
                sendEvent(name: "weatherIcon", value: resp2.data.forecast.forecastday.day[1].condition.icon, isStateChange: true)
                }
                if(iconType == true){ 
		        sendEvent(name: "weatherIcon", value: resp2.data.current.condition.icon, isStateChange: true)
                }    
         
            
    // Apixu With Units ***************************************************************
            
          if(state.DisplayUnits == true){
                       
           if(rainUnit == "IN"){
                

               
           
          sendEvent(name: "rainTomorrow", value: resp2.data.forecast.forecastday.day[1].totalprecip_in +state.RU, isStateChange: true)
          sendEvent(name: "rainDayAfterTomorrow", value: resp2.data.forecast.forecastday.day[2].totalprecip_in +state.RU, isStateChange: true)
             
           }
               
           
          if(rainUnit == "MM"){ 
 
           
          sendEvent(name: "rainTomorrow", value: resp2.data.forecast.forecastday.day[1].totalprecip_mm +state.RU, isStateChange: true)
          sendEvent(name: "rainDayAfterTomorrow", value: resp2.data.forecast.forecastday.day[2].totalprecip_mm +state.RU, isStateChange: true)
             
           }
            
          if(temperatureUnit == "Celsius (�C)"){
    
      	
            sendEvent(name: "forecastHigh", value: resp2.data.forecast.forecastday.day[0].maxtemp_c +state.TU, isStateChange: true)
            sendEvent(name: "forecastLow", value: resp2.data.forecast.forecastday.day[0].mintemp_c +state.TU, isStateChange: true)              
           
          }
              
          if(temperatureUnit == "Fahrenheit (�F)"){ 

               
         sendEvent(name: "forecastHigh", value: resp2.data.forecast.forecastday.day[0].maxtemp_f +state.TU, isStateChange: true)
   	     sendEvent(name: "forecastLow", value: resp2.data.forecast.forecastday.day[0].mintemp_f +state.TU, isStateChange: true)
                    
           }  
            
  
              
          }      
              
     // Apixu Without Units ***************************************************************
          if(state.DisplayUnits == false){
              
           if(rainUnit == "IN"){

          
          sendEvent(name: "rainTomorrow", value: resp2.data.forecast.forecastday.day[1].totalprecip_in, unit:"in", isStateChange: true)
          sendEvent(name: "rainDayAfterTomorrow", value: resp2.data.forecast.forecastday.day[2].totalprecip_in, unit:"in", isStateChange: true) 
               }    
               
                  
          if(rainUnit == "MM"){  

          sendEvent(name: "rainTomorrow", value: resp2.data.forecast.forecastday.day[1].totalprecip_mm, unit:"mm", isStateChange: true)
          sendEvent(name: "rainDayAfterTomorrow", value: resp2.data.forecast.forecastday.day[2].totalprecip_mm, unit:"mm", isStateChange: true)
              }    

            
          if(temperatureUnit == "Celsius (�C)"){

              
                    
          sendEvent(name: "forecastHigh", value: resp2.data.forecast.forecastday.day[0].maxtemp_c, unit:"C", isStateChange: true)
          sendEvent(name: "forecastLow", value: resp2.data.forecast.forecastday.day[0].mintemp_c, unit:"C", isStateChange: true)
             
           }
              
          if(temperatureUnit == "Fahrenheit (�F)"){ 

          
          sendEvent(name: "forecastHigh", value: resp2.data.forecast.forecastday.day[0].maxtemp_f, unit:"F", isStateChange: true)
           sendEvent(name: "forecastLow", value: resp2.data.forecast.forecastday.day[0].mintemp_f, unit:"F", isStateChange: true)
            }   
              
           
          if(distanceFormat == "Miles (mph)"){  
          sendEvent(name: "visibility", value: resp2.data.current.vis_miles, unit: "mi", isStateChange: true)
           }  
            
          if(distanceFormat == "Kilometres (kph)"){
          sendEvent(name: "visibility", value: resp2.data.current.vis_km, unit: "km", isStateChange: true)
           }
              
          }
                  
              
              
              
              
              
              
              
              
              
              
              
              
              
              
              
              
              
              
              
              
         
     
// END: Get APIXU data *******************************************************           
            
            
           

        
    
       } 
        
    } catch (e) {
        log.error "something went wrong: $e"
    }
    
}




def pollSchedule1()
{
    PollApixuNow()
}


def pollSchedule()
{
    PollStationNow()
}
              
def parse(String description) {
}


def PollStationNow()
{
    units()
    state.DriverVersion = "1.0.0"   
    // ************************* Update as required *************************************
    
 
    log.debug "Weewx: ForcePoll called"
    def params1 = [
        uri: "http://${ipaddress}:${weewxPort}/${weewxPath}"
         ]
    
    try {
        httpGet(params1) { resp1 ->
            resp1.headers.each {
            log.debug "Response1: ${it.name} : ${it.value}"
        }
            if(logSet == true){  
           
            log.debug "params1: ${params1}"
            log.debug "response contentType: ${resp1.contentType}"
 		    log.debug "response data: ${resp1.data}"
            } 
            
            
            if(logSet == false){ 
            log.info "Further Weewx detailed data logging disabled"    
            }    
   
           
            
  // Conversions
            
            // inHg to mbar - multiply inches by 33.8638815 to get mbar
            // Example: (50�F - 32) x .5556 = 10�C   *********** def Celcius1=(Faran =32) *0.5556
            // 1 mi = 1.609344 km -   ************  def newKM=miles * 1.609344
            //   = (68�F - 32) � 5/9 = 20 �C
            
            
            // def convertToC = (Faran1 - 32) *0.5556
            // def convertToKM = miles * 1.609344
            // def convertToMbar = inHg1 * 33.8638815
            
// Collect Data
           
 // ************************ ILLUMINANCE **************************************************************************************           
              def illuminanceRaw1 = (resp1.data.stats.current.solarRadiation.replaceFirst(wmcode, ""))
               	if(illuminanceRaw1.contains("N/A") || illuminanceRaw1 == null){
                state.Illuminance = 'No Station Data'}
            else{
                state.Illuminance = illuminanceRaw1
               
                }
// ************************* SOLAR RADIATION*****************************************************************************************           
            
              def solarradiationRaw1 = (resp1.data.stats.current.solarRadiation.replaceFirst(wmcode, ""))
            	if(solarradiationRaw1.contains("N/A") || solarradiationRaw1 == null ){
                  	state.SolarRadiation = 'No Station Data'}
            	else{
                     state.SolarRadiation = solarradiationRaw1
                }
            
// ************************** HUMIDITY ***************************************************************************************   
            
              def humidityRaw1 = (resp1.data.stats.current.humidity.replaceFirst("%", ""))
            	if(humidityRaw1.contains("N/A") || humidityRaw1 == null){
                state.Humidity = 'No Station Data'}
            	else{
                   state.Humidity = humidityRaw1
                }

// ************************** INSIDE HUMIDITY ************************************************************************************
            
              def inHumidRaw1 = (resp1.data.stats.current.insideHumidity.replaceFirst("%", "")) 
            	if(inHumidRaw1.contains("N/A") || inHumidRaw1 ==null){
                   
                	state.InsideHumidity = 'No Station Data'}
            	else{
                    
                	state.InsideHumidity = inHumidRaw1
                }
                        
            
// ************************* DEWPOINT *****************************************************************************************
            
                def dewpointRaw1 = (resp1.data.stats.current.dewpoint)
                 	if(dewpointRaw1.contains("N/A") || dewpointRaw1 == null){
                    state.Dewpoint = 'No Station Data'}
            
            	if (dewpointRaw1.contains("F")) {
                dewpointRaw1 = dewpointRaw1.replace(fcode, "")
                    
                if(temperatureUnit == "Fahrenheit (�F)"){
            	state.TU = ' �F'
                state.Dewpoint = dewpointRaw1
                log.info "Dewpoint Input = F - Output = F -- No conversion required"
                }    
                if(temperatureUnit == "Celsius (�C)"){
                state.TU = ' �C'
                def dewpoint1 = convertFtoC(dewpointRaw1) 
                state.Dewpoint = dewpoint1 
                   
                }    

            } 
            
           		if (dewpointRaw1.contains("C")) {
                dewpointRaw1 = dewpointRaw1.replace(ccode, "")
                    
                 if(temperatureUnit == "Fahrenheit (�F)"){
            	state.TU = ' �F'
                def dewpoint1 = convertCtoF(dewpointRaw1)    
                state.Dewpoint = dewpoint1 
                }    
                 if(temperatureUnit == "Celsius (�C)"){
                state.TU = ' �C'
                state.Dewpoint = dewpointRaw1
                 log.info "Dewpoint Input = C - Output = C -- No conversion required"  
                }        

            } 
            
            
            
            
            

// ************************** PRESSURE ****************************************************************************************            
           
              def pressureRaw1 = (resp1.data.stats.current.barometer)
                    if (pressureRaw1.contains("N/A") || insideTemperatureRaw1 == null){
                    state.Pressure = 'No Station Data'}
            
            if (pressureRaw1.contains("inHg")) {
                pressureRaw1 = pressureRaw1.replace("inHg", "")
                
                if(pressureUnit == "INHg"){
            	state.PU = ' inhg'
                state.Pressure = pressureRaw1
                log.info "Pressure Input = INHg - Output = INHg -- No conversion required"
                }
                
                if(pressureUnit == "MBAR"){
                state.PU = ' mbar'
                def pressureTemp1 = convertINtoMB(pressureRaw1) 
                state.Pressure = pressureTemp1 
                
                }
                
            } 
            
            if (pressureRaw1.contains("mbar")) {
                 pressureRaw1 = pressureRaw1.replace("mbar", "")
                
            	if(pressureUnit == "INHg"){
            	state.PU = ' inhg'
                def pressureTemp1 = convertMBtoIN(pressureRaw1)
                state.Pressure = pressureTemp1
                }
                 if(pressureUnit == "MBAR"){
                 state.PU = ' mbar'
                 state.Pressure = pressureRaw1 
                 log.info "Pressure Input = MBAR - Output = MBAR --No conversion required"
                }
                
            } 
            
            
         
            
// ************************** WIND SPEED ****************************************************************************************
            
    		  def windSpeedRaw1 = (resp1.data.stats.current.windSpeed) 
            if(windSpeedRaw1.contains("N/A") || windSpeedRaw1 == null){
                    state.WindSpeed = 'No Station Data'}
            
            if (windSpeedRaw1.contains("mph")) {
                windSpeedRaw1 = windSpeedRaw1.replace("mph", "")
                
                if(speedUnit == "MPH"){
            	state.SU = ' mph'
                state.WindSpeed = windSpeedRaw1
                log.info "Wind Speed Input = MPH - Output = MPH -- No conversion required"
                }
                
                if(speedUnit == "KPH"){
                state.SU = ' kph'
                def speedTemp1 = convertMPHtoKPH(windSpeedRaw1) 
                state.WindSpeed = speedTemp1 
            
                }
                
            } 
            
            if (windSpeedRaw1.contains("kph")) {
                 windSpeedRaw1 = windSpeedRaw1.replace("kph", "")
                
            	if(speedUnit == "MPH"){
            	state.SU = ' mph'
                def speedTemp1 = convertKPHtoMPH(pressureRaw1)
                state.WindSpeed = speedTemp1
                }
                 if(speedUnit == "KPH"){
                 state.SU = ' kph'
                 state.WindSpeed = windSpeedRaw1 
                 log.info "WindSpeed Input = KPH - Output = KPH --No conversion required"
                }
                
            } 
            
                   
// ************************** WIND GUST ****************************************************************************************
            
              def windGustRaw1 = (resp1.data.stats.current.windGust)  
            	 if(windGustRaw1.contains("N/A") || windGustRaw1 == null){
                    state.WindGust = 'No Station Data'}
            
            if (windGustRaw1.contains("mph")) {
                windGustRaw1 = windGustRaw1.replace("mph", "")
                
                if(speedUnit == "MPH"){
            	state.SU = ' mph'
                state.WindGust = windGustRaw1
                log.info "Wind Gust Speed Input = MPH - Output = MPH -- No conversion required"
                }
                
                if(speedUnit == "KPH"){
                state.SU = ' kph'
                def speedTemp2 = convertMPHtoKPH(windGustRaw1) 
                state.WindGust = speedTemp2 
            
                }
                
            } 
            
            if (windGustRaw1.contains("kph")) {
                 windGustRaw1 = windGustRaw1.replace("kph", "")
                
            	if(speedUnit == "MPH"){
            	state.SU = ' mph'
                def speedTemp2 = convertKPHtoMPH(windGustRaw1)
                state.WindGust = speedTemp2
                }
                 if(speedUnit == "KPH"){
                 state.SU = ' kph'
                 state.WindGust = windGustRaw1 
                 log.info "Wind Gust Speed Input = KPH - Output = KPH --No conversion required"
                }
                
            } 
            
// ************************** INSIDE TEMP **************************************************************************************** 
          
              def insideTemperatureRaw1 = (resp1.data.stats.current.insideTemp)
                    if (insideTemperatureRaw1.contains("N/A") || insideTemperatureRaw1 == null){
                    state.insideTemp = 'No Station Data'}
            
            if (insideTemperatureRaw1.contains("F")) {
                insideTemperatureRaw1 = insideTemperatureRaw1.replace(fcode, "")
                
                if(temperatureUnit == "Fahrenheit (�F)"){
            	state.TU = ' �F'
                state.InsideTemp = insideTemperatureRaw1
                log.info "InsideTemperature Input = F - Output = F -- No conversion required"
                }
                
                if(temperatureUnit == "Celsius (�C)"){
                state.TU = ' �C'
                def insideTemp1 = convertFtoC(insideTemperatureRaw1) 
                state.InsideTemp = insideTemp1 
                
                }
                
            } 
            
            if (insideTemperatureRaw1.contains("C")) {
                insideTemperatureRaw1 = insideTemperatureRaw1.replace(ccode, "")
                
            	if(temperatureUnit == "Fahrenheit (�F)"){
            	state.TU = ' �F'
                def insideTemp1 = convertCtoF(insideTemperatureRaw1)
                state.InsideTemp = insideTemp1
                }
                if(temperatureUnit == "Celsius (�C)"){
                state.TU = ' �C'
                state.InsideTemp = insideTemperatureRaw1  
                    log.info "InsideTemperature Input = C - Output = C --No conversion required"
                }
                
            } 
  
// ************************** RAIN RATE ****************************************************************************************    
            
            
            def rainRateRaw1 = (resp1.data.stats.current.rainRate) 
            	if(rainRateRaw1.contains("N/A") || rainRateRaw1 == null){
                   state.Rainrate = 'No Station Data'}
            	
            if(rainRateRaw1.contains("in/hr")){
                rainRateRaw1 = rainRateRaw1.replace("in/hr", "")
                
                if(rainUnit == "IN"){
                    state.RRU = " in/hr"
                 	state.Rainrate = rainRateRaw1  
                     log.info "Rainrate Input = in/hr - Output = in/hr --No conversion required"
                }
            
            	if(rainUnit == "MM"){
            		state.RRU = " mm/hr"
                    rrTemp = convertINtoMM(rainRateRaw1)
                    state.Rainrate = rrTemp
            }
            }
            
             if(rainRateRaw1.contains("mm/hr")){
                rainRateRaw1 = rainRateRaw1.replace("mm/hr", "")
                0.621371
                if(rainUnit == "IN"){
                    state.RRU = " in/hr"
                    rrTemp = convertMMtoIN(rainRateRaw1)
                 	state.Rainrate = rrTemp 
                }
            
            	if(rainUnit == "MM"){
            		state.RRU = " mm/hr"
                   state.Rainrate = rainRateRaw1 
                   log.info "Rainrate Input = mm/hr - Output = mm/hr --No conversion required"
            }
            }
            

// ************************** RAIN TODAY ****************************************************************************************    
            
              def rainTodayRaw1 = (resp1.data.stats.sinceMidnight.rainSum)
               	if(rainTodayRaw1.contains("N/A") || rainTodayRaw1 == null){
                   state.RainToday = 'No Station Data'}
            	
            if(rainTodayRaw1.contains("in")){
                rainTodayRaw1 = rainTodayRaw1.replace("in", "")
                
                if(rainUnit == "IN"){
                    state.RU = " in"
                 	state.RainToday = rainTodayRaw1 
                     log.info "RainToday Input = in - Output = in --No conversion required"
                }
            
            	if(rainUnit == "MM"){
            		state.RU = " mm"
                    rtTemp = convertINtoMM(rainTodayRaw1)
                    state.RainToday = rtTemp
            }
            }
            
             if(rainTodayRaw1.contains("mm")){
                rainTodayRaw1 = rainTodayRaw1.replace("mm", "")
                
                if(rainUnit == "IN"){
                    state.RU = " in"
                    rtTemp = convertMMtoIN(rainTodayRaw1)
                 	state.RainToday = rtTemp 
                }
            
            	if(rainUnit == "MM"){
            		state.RU = " mm"
                   state.RainToday = rainTodayRaw1 
                   log.info "RainToday Input = mm - Output = mm --No conversion required"
            }
            }


// ************************** TEMPERATURE ****************************************************************************************
            
              def temperatureRaw1 = (resp1.data.stats.current.outTemp) 
            	if(temperatureRaw1.contains("N/A") || temperatureRaw1 ==null){
                state.Temperature = 'No Station Data'}
            
            if (temperatureRaw1.contains("F")) {
                temperatureRaw1 = temperatureRaw1.replace(fcode, "")
                
                if(temperatureUnit == "Fahrenheit (�F)"){
            	state.TU = ' �F'
                state.Temperature = temperatureRaw1
                log.info "Temperature Input = F - Output = F -- No conversion required"
                }
                
                if(temperatureUnit == "Celsius (�C)"){
                state.TU = ' �C'
                def temp1 = convertFtoC(temperatureRaw1) 
                state.Temperature = temp1 
                
                }
                
            } 
            
            if (temperatureRaw1.contains("C")) {
                temperatureRaw1 = temperatureRaw1.replace(ccode, "")
                
            	if(temperatureUnit == "Fahrenheit (�F)"){
            	state.TU = ' �F'
                    def temp1 = convertCtoF(temperatureRaw1)
                state.Temperature = temp1
                }
                if(temperatureUnit == "Celsius (�C)"){
                state.TU = ' �C'
                state.Temperature = temperatureRaw1  
                    log.info "Temperature Input = C - Output = C --No conversion required"
                }
                
            } 
           
                    
                    
                    
                    
                    
                    
                    
  
            
// ************************** uv ************************************************************************************************            
            
              def UVRaw1 = (resp1.data.stats.current.UV)
            	if(UVRaw1.contains("N/A") || UVRaw1 ==null){
                   
                	state.UV = 'No Station Data'}
            	else{
                    state.UV = UVRaw1
                
                    if(state.UV <= '2.9'){state.UVHarm = 'Low'}
                    if(state.UV >= '3.0' && state.UV <= '5.9'){state.UVHarm = 'Moderate'}
            		if(state.UV >= '6.0' && state.UV <= '7.9'){state.UVHarm = 'High'}
 					if(state.UV >= '8.0' && state.UV <= '10.9'){state.UVHarm = 'Very High'}
					if(state.UV >= '11.0'){state.UVHarm = 'Extreme'}



                } 
            
            
            
            
            

// ************************** WINDCHILL ****************************************************************************************            
            
              def windChillRaw1 = (resp1.data.stats.current.windchill)
            	if(windChillRaw1.contains("N/A") || windChillRaw1 ==null){
                   state.FeelsLike = 'No Station Data'}
                	
            
 				if (windChillRaw1.contains("F")) {
                windChillRaw1 = windChillRaw1.replace(fcode, "")
                
                if(temperatureUnit == "Fahrenheit (�F)"){
            	state.TU = ' �F'
                state.FeelsLike = windChillRaw1
                log.info "FeelsLike Input = F - Output = F -- No conversion required"
                }
                
                if(temperatureUnit == "Celsius (�C)"){
                state.TU = ' �C'
                def feelslike1 = convertFtoC(windChillRaw1) 
                state.FeelsLike = feelslike1
                
                }
                
            } 
            
            if (windChillRaw1.contains("C")) {
                windChillRaw1 = windChillRaw1.replace(ccode, "")
                             
            	if(temperatureUnit == "Fahrenheit (�F)"){
            	state.TU = ' �F'
                def feelslike1 = convertCtoF(windChillRaw1)
                state.FeelsLike = feelslike1
                }
                if(temperatureUnit == "Celsius (�C)"){
                state.TU = ' �C'
                state.FeelsLike = windChillRaw1 
                    log.info "FeelsLike Input = C - Output = C --No conversion required"
                }
                
            } 
           
                    
                    
                    
            
   
             //  any more?
                 
             		
            
      
        
    
            
 // Basics - No units ************************************************************************************************
            
             sendEvent(name: "DriverAuthor", value: "Cobra", isStateChange: true)
             sendEvent(name: "DriverVersion", value: state.DriverVersion, isStateChange: true)
             sendEvent(name: "WeewxServerUptime", value: resp1.data.serverUptime, isStateChange: true)
             sendEvent(name: "WeewxServerLocation", value: resp1.data.location, isStateChange: true)
             sendEvent(name: "sunrise", value: resp1.data.almanac.sun.sunrise, isStateChange: true)
             sendEvent(name: "sunset", value: resp1.data.almanac.sun.sunset, isStateChange: true)
             sendEvent(name: "moonPhase", value: resp1.data.almanac.moon.phase, isStateChange: true)
             sendEvent(name: "moonRise", value: resp1.data.almanac.moon.rise, isStateChange: true)
             sendEvent(name: "uv", value: state.UV, isStateChange: true)
             sendEvent(name: "uvHarm", value: state.UVHarm, isStateChange: true)
             sendEvent(name: "LastUpdate-Weewx", value: resp1.data.time, isStateChange: true)
            
            
            
            
            
                      
            
            def windDirRaw = (resp1.data.stats.current.windDirText)
            	if(windDirRaw){
                    if(windDirRaw.contains("N/A")){sendEvent(name: "wind_dir", value:"No Station Data", isStateChange: true)}
                    else {sendEvent(name: "wind_dir", value: windDirRaw, isStateChange: true)} 
                
                }
         
             def pressureTrend = (resp1.data.stats.current.barometerTrendData) 
                  if(pressureTrend){
                      if(pressureTrend.contains("N/A")){sendEvent(name: "pressure_trend", value:"No Station Data", isStateChange: true)}
                      else if(pressureTrend.contains("-")){sendEvent(name: "pressure_trend", value:"Falling", isStateChange: true)} 
                      else if(pressureTrend.contains("+")){sendEvent(name: "pressure_trend", value:"Rising", isStateChange: true)} 
                      else {sendEvent(name: "pressure_trend", value:"Static", isStateChange: true)} 
                  }
            
            
            
            
// // Send Events  - WITH UNITS ********************************************************************************************            
              if(state.DisplayUnits == true){  
                         
                  sendEvent(name: "illuminance", value: state.Illuminance + state.IU, isStateChange: true)
                  sendEvent(name: "solarradiation", value: state.SolarRadiation + state.SRU, isStateChange: true)
                  sendEvent(name: "dewpoint", value: state.Dewpoint + state.TU, isStateChange: true)
                  sendEvent(name: "humidity", value: state.Humidity + state.HU, isStateChange: true)
                  sendEvent(name: "pressure", value: state.Pressure + state.PU, isStateChange: true) 
                  sendEvent(name: "wind", value: state.WindSpeed + state.SU, isStateChange: true)
                  sendEvent(name: "wind_gust", value: state.WindGust + state.SU, isStateChange: true)
                  sendEvent(name: "inside_temperature", value: state.InsideTemp + state.TU, isStateChange: true)
                  sendEvent(name: "inside_humidity", value: state.InsideHumidity + state.HU, isStateChange: true)  
                  sendEvent(name: "temperature", value: state.Temperature + state.TU, isStateChange: true)
                  sendEvent(name: "rain_rate", value: state.Rainrate + state.RRU, isStateChange: true)
                  sendEvent(name: "precip_today", value: state.RainToday + state.RU, isStateChange: true) 
                  sendEvent(name: "precip_1hr", value: state.Rainrate + state.RU, isStateChange: true)
                  sendEvent(name: "feelsLike", value: state.FeelsLike + state.TU, isStateChange: true)
                  
                  
    
                  
              }
            
// // Send Events  - WITHOUT UNITS *****************************************************************************************               
             if(state.DisplayUnits == false){  
                  sendEvent(name: "illuminance", value: state.Illuminance, isStateChange: true)
                  sendEvent(name: "solarradiation", value: state.SolarRadiation, isStateChange: true)
                  sendEvent(name: "dewpoint", value: state.Dewpoint, isStateChange: true)
                  sendEvent(name: "humidity", value: state.Humidity, isStateChange: true)
                  sendEvent(name: "pressure", value: state.Pressure, isStateChange: true)
                  sendEvent(name: "wind", value: state.WindSpeed , isStateChange: true)
                  sendEvent(name: "wind_gust", value: state.WindGust, isStateChange: true)
                  sendEvent(name: "inside_temperature", value: state.InsideTemp, isStateChange: true)
                  sendEvent(name: "inside_humidity", value: state.InsideHumidity, isStateChange: true)   
                  sendEvent(name: "temperature", value: state.Temperature, isStateChange: true)
                  sendEvent(name: "rain_rate", value: state.Rainrate, isStateChange: true)  
                  sendEvent(name: "precip_today", value: state.RainToday, isStateChange: true)  
                  sendEvent(name: "precip_1hr", value: state.Rainrate, isStateChange: true)  
                  sendEvent(name: "feelsLike", value: state.FeelsLike, isStateChange: true) 
            

        }
            
// **********************************************************************************************            
  
         
           
           def WeatherSummeryFormat = weatherFormat
            
            if(summaryType == true){
            
            if (WeatherSummeryFormat == "Celsius, Miles & MPH"){
                		 sendEvent(name: "weatherSummaryFormat", value: "Celsius, Miles & MPH", isStateChange: true)
                         sendEvent(name: "weatherSummary", value: "Weather summary for" + " " + resp1.data.current_observation.display_location.city + ", " + resp1.data.current_observation.observation_time+ ". "   
                       + resp1.data.forecast.simpleforecast.forecastday[0].conditions + " with a high of " + resp1.data.forecast.simpleforecast.forecastday[0].high.celsius + " degrees, " + "and a low of " 
                       + resp1.data.forecast.simpleforecast.forecastday[0].low.celsius  + " degrees. " + "Humidity is currently around " + resp1.data.current_observation.relative_humidity + " and temperature is " 
                       + resp1.data.current_observation.temp_c + " degrees. " + " The temperature feels like it's " + resp1.data.current_observation.feelslike_c + " degrees. " + "Wind is from the " + resp1.data.current_observation.wind_dir
                       + " at " + resp1.data.current_observation.wind_mph + " mph" + ", with gusts up to " + resp1.data.current_observation.wind_gust_mph + " mph" + ". Visibility is around " 
                       + resp1.data.current_observation.visibility_mi + " miles" + ". " + "There is a "+resp1.data.forecast.simpleforecast.forecastday[0].pop + "% chance of rain today." , isStateChange: true
                      )  
            }
                
            if (WeatherSummeryFormat == "Fahrenheit, Miles & MPH"){
                 		 sendEvent(name: "weatherSummaryFormat", value: "Fahrenheit, Miles & MPH", isStateChange: true)
                         sendEvent(name: "weatherSummary", value: "Weather summary for" + " " + resp1.data.current_observation.display_location.city + ", " + resp1.data.current_observation.observation_time+ ". "  
                       + resp1.data.forecast.simpleforecast.forecastday[0].conditions + " with a high of " + resp1.data.forecast.simpleforecast.forecastday[0].high.fahrenheit + " degrees, " + "and a low of " 
                       + resp1.data.forecast.simpleforecast.forecastday[0].low.fahrenheit  + " degrees. " + "Humidity is currently around " + resp1.data.current_observation.relative_humidity + " and temperature is " 
                       + resp1.data.current_observation.temp_f + " degrees. " + " The temperature feels like it's " + resp1.data.current_observation.feelslike_f + " degrees. " + "Wind is from the " + resp1.data.current_observation.wind_dir
                       + " at " + resp1.data.current_observation.wind_mph + " mph" + ", with gusts up to: " + resp1.data.current_observation.wind_gust_mph + " mph" + ". Visibility is around " 
                       + resp1.data.current_observation.visibility_mi + " miles" + ". " + "There is a "+resp1.data.forecast.simpleforecast.forecastday[0].pop + "% chance of rain today." , isStateChange: true
                      )  
            }   
                
             if (WeatherSummeryFormat == "Celsius, Kilometres & KPH"){
                 		 sendEvent(name: "weatherSummaryFormat", value: "Celsius, Kilometres & KPH", isStateChange: true)
                         sendEvent(name: "weatherSummary", value: "Weather summary for" + " " + resp1.data.current_observation.display_location.city + ", " + resp1.data.current_observation.observation_time+ ". "  
                       + resp1.data.forecast.simpleforecast.forecastday[0].conditions + " with a high of " + resp1.data.forecast.simpleforecast.forecastday[0].high.celsius + " degrees, " + "and a low of " 
                       + resp1.data.forecast.simpleforecast.forecastday[0].low.celsius  + " degrees. " + "Humidity is currently around " + resp1.data.current_observation.relative_humidity + " and temperature is " 
                       + resp1.data.current_observation.temp_c + " degrees. " + " The temperature feels like it's " + resp1.data.current_observation.feelslike_c + " degrees. " + "Wind is from the " + resp1.data.current_observation.wind_dir
                       + " at " + resp1.data.current_observation.wind_kph + " kph" + ", with gusts up to " + resp1.data.current_observation.wind_gust_kph + " kph" + ". Visibility is around " 
                       + resp1.data.current_observation.visibility_km + " kilometres" + ". " + "There is a "+resp1.data.forecast.simpleforecast.forecastday[0].pop + "% chance of rain today." , isStateChange: true
                      )  
            }
                
                
        }    
            
            
            
            
            
            
            
            if(summaryType == false){
                
             if (WeatherSummeryFormat == "Celsius, Miles & MPH"){
                		 sendEvent(name: "weatherSummaryFormat", value: "Celsius, Miles & MPH", isStateChange: true)
                         sendEvent(name: "weatherSummary", value: resp1.data.forecast.simpleforecast.forecastday[0].conditions + ". " + " Forecast High:" + resp1.data.forecast.simpleforecast.forecastday[0].high.celsius + ", Forecast Low:" 
                       + resp1.data.forecast.simpleforecast.forecastday[0].low.celsius  +  ". Humidity: " + resp1.data.current_observation.relative_humidity + " Temperature: " 
                       + resp1.data.current_observation.temp_c  + ". Wind Direction: " + resp1.data.current_observation.wind_dir + ". Wind Speed: " + resp1.data.current_observation.wind_mph + " mph" 
                       + ", Gust: " + resp1.data.current_observation.wind_gust_mph + " mph. Rain: "  +resp1.data.forecast.simpleforecast.forecastday[0].pop + "%" , isStateChange: true
                      )  
            }
            
            if (WeatherSummeryFormat == "Fahrenheit, Miles & MPH"){
                		 sendEvent(name: "weatherSummaryFormat", value: "Fahrenheit, Miles & MPH", isStateChange: true)
                         sendEvent(name: "weatherSummary", value: resp1.data.forecast.simpleforecast.forecastday[0].conditions + ". " + " Forecast High:" + resp1.data.forecast.simpleforecast.forecastday[0].high.fahrenheit + ", Forecast Low:" 
                       + resp1.data.forecast.simpleforecast.forecastday[0].low.fahrenheit  +  ". Humidity: " + resp1.data.current_observation.relative_humidity + " Temperature: " 
                       + resp1.data.current_observation.temp_f  + ". Wind Direction: " + resp1.data.current_observation.wind_dir + ". Wind Speed: " + resp1.data.current_observation.wind_mph + " mph" 
                       + ", Gust: " + resp1.data.current_observation.wind_gust_mph + " mph. Rain:"  +resp1.data.forecast.simpleforecast.forecastday[0].pop + "%", isStateChange: true
                      )  
            }
            
             if (WeatherSummeryFormat ==  "Celsius, Kilometres & KPH"){
                		 sendEvent(name: "weatherSummaryFormat", value:  "Celsius, Kilometres & KPH", isStateChange: true)
                         sendEvent(name: "weatherSummary", value: resp1.data.forecast.simpleforecast.forecastday[0].conditions + ". " + " Forecast High:" + resp1.data.forecast.simpleforecast.forecastday[0].high.celsius + ", Forecast Low:" 
                       + resp1.data.forecast.simpleforecast.forecastday[0].low.celsius  +  ". Humidity: " + resp1.data.current_observation.relative_humidity + " Temperature: " 
                       + resp1.data.current_observation.temp_c  + ". Wind Direction: " + resp1.data.current_observation.wind_dir + ". Wind Speed: " + resp1.data.current_observation.wind_kph + " kph" 
                       + ", Gust: " + resp1.data.current_observation.wind_gust_kph + " kph. Rain:"  +resp1.data.forecast.simpleforecast.forecastday[0].pop + "%", isStateChange: true
                      )  
            }
            
            }    
            
            
    
            

                
    
       //     sendEvent(name: "observation_time", value: resp1.data.current_observation.observation_time, isStateChange: true)
      //      sendEvent(name: "weather", value: resp1.data.current_observation.weather, isStateChange: true)
  		//    sendEvent(name: "wind_string", value: resp1.data.current_observation.wind_string)
       //     sendEvent(name: "forecastConditions", value: resp1.data.forecast.simpleforecast.forecastday[0].conditions, isStateChange: true)
      
            
            
     //       if(rainFormat == "Inches"){
      //      sendEvent(name: "precip_1hr", value: resp1.data.current_observation.precip_1hr_in, unit: "IN", isStateChange: true)
       //     sendEvent(name: "precip_today", value: resp1.data.current_observation.precip_today_in, unit: "IN", isStateChange: true)
      //      sendEvent(name: "rainTomorrow", value: resp1.data.forecast.simpleforecast.forecastday[1].qpf_allday.in, unit: "IN", isStateChange: true)
       //     sendEvent(name: "rainDayAfterTomorrow", value: resp1.data.forecast.simpleforecast.forecastday[2].qpf_allday.in, unit: "IN", isStateChange: true)
      //      sendEvent(name: "rainUnit", value: "Inches", isStateChange: true)
    //        }
      //      if(rainFormat == "Millimetres"){   
      //      sendEvent(name: "precip_today", value: resp1.data.current_observation.precip_today_metric, unit: "MM", isStateChange: true)
      //      sendEvent(name: "precip_1hr", value: resp1.data.current_observation.precip_1hr_metric, unit: "MM", isStateChange: true)
     //       sendEvent(name: "rainTomorrow", value: resp1.data.forecast.simpleforecast.forecastday[1].qpf_allday.mm, unit: "MM", isStateChange: true)
     //       sendEvent(name: "rainDayAfterTomorrow", value: resp1.data.forecast.simpleforecast.forecastday[2].qpf_allday.mm, unit: "MM", isStateChange: true)
     //       sendEvent(name: "rainUnit", value: "Millimetres", isStateChange: true)
    //        }
            
      //      if(tempFormat == "Celsius"){
      //      sendEvent(name: "dewpoint", value: resp1.data.current_observation.dewpoint_c, unit: "C", isStateChange: true)
      //      sendEvent(name: "forecastHigh", value: resp1.data.forecast.simpleforecast.forecastday[0].high.celsius, unit: "C", isStateChange: true)
      //      sendEvent(name: "forecastLow", value: resp1.data.forecast.simpleforecast.forecastday[0].low.celsius, unit: "C", isStateChange: true)
      //      sendEvent(name: "temperatureUnit", value: "Celsius", isStateChange: true)
      //      sendEvent(name: "feelsLike", value: resp1.data.current_observation.feelslike_c, unit: "C", isStateChange: true)   
     //       sendEvent(name: "temperature", value: resp1.data.current_observation.temp_c, unit: "C", isStateChange: true)
         
            	
     //   }
      //     if(tempFormat == "Fahrenheit"){ 
     //      sendEvent(name: "temperature", value: resp1.data.current_observation.temp_f, unit: "F", isStateChange: true)
     //      sendEvent(name: "feelsLike", value: resp1.data.current_observation.feelslike_f, unit: "F", isStateChange: true)
     //      sendEvent(name: "dewpoint", value: resp1.data.current_observation.dewpoint_f, unit: "F", isStateChange: true)
     //      sendEvent(name: "forecastHigh", value: resp1.data.forecast.simpleforecast.forecastday[0].high.fahrenheit, unit: "F", isStateChange: true)
     //      sendEvent(name: "forecastLow", value: resp1.data.forecast.simpleforecast.forecastday[0].low.fahrenheit, unit: "F", isStateChange: true)
     //      sendEvent(name: "temperatureUnit", value: "Fahrenheit", isStateChange: true)
     //      sendEvent(name: "feelsLike", value: resp1.data.current_observation.feelslike_f, unit: "F", isStateChange: true)    
     //      sendEvent(name: "temperature", value: resp1.data.current_observation.temp_f, unit: "F", isStateChange: true)	
    	
   //        }  
            
    //      if(distanceFormat == "Miles (mph)"){  
      //      sendEvent(name: "visibility", value: resp1.data.current_observation.visibility_mi, unit: "mi", isStateChange: true)
     //       sendEvent(name: "wind", value: resp1.data.current_observation.wind_mph, unit: "MPH", isStateChange: true)
       //     sendEvent(name: "wind_gust", value: resp1.data.current_observation.wind_gust_mph, isStateChange: true) 
     //       sendEvent(name: "distanceUnit", value: "Miles (mph)", isStateChange: true)
     //     }  
            
     //     if(distanceFormat == "Kilometres (kph)"){
     //      sendEvent(name: "visibility", value: resp1.data.current_observation.visibility_km, unit: "km", isStateChange: true)
     //      sendEvent(name: "wind", value: resp1.data.current_observation.wind_kph, unit: "KPH", isStateChange: true)  
     //      sendEvent(name: "wind_gust", value: resp1.data.current_observation.wind_gust_kph, isStateChange: true) 
     //      sendEvent(name: "distanceUnit", value: "Kilometres (kph)", isStateChange: true)  
     //     }
                      
     //       if(pressureFormat == "Inches"){
                
     //       sendEvent(name: "pressure", value: resp1.data.current_observation.pressure_in, unit: "mi", isStateChange: true)
      //      sendEvent(name: "pressureUnit", value: "Inches")  
      //      }
            
      //      if(pressureFormat == "Millibar"){
      //      sendEvent(name: "pressure", value: resp1.data.current_observation.pressure_mb, unit: "mb", isStateChange: true)
      //      sendEvent(name: "pressureUnit", value: "Millibar", isStateChange: true) 
      //      }
            
   

               
      //    state.lastPoll = now()     
   } 
        
    } catch (e) {
        log.error "something went wrong: $e"
    }
    
}


def getFcode(){
     def charF1 ="&-#-1-7-6-;-F"
     def charF = charF1.replace("-", "")
return charF
}

def getCcode(){
    def charC1 ="&-#-1-7-6-;-C"
    def charC = charC1.replace("-", "")
return charC
}

def getWmcode(){
     def wm1 ="W-/-m-&-#-1-7-8;"
    def wm = wm1.replace("-", "")
return wm
}

def convertFtoC(temperatureIn){
    log.info "Converting F to C"
    def tempIn = temperatureIn.toFloat()
    log.info "tempIn = $tempIn"
    def tempCalc = ((tempIn - 32) *0.5556)  
    def tempOut1 = tempCalc.round(state.DecimalPlaces)
    def tempOut = tempOut1
    log.info "tempOut =  $tempOut"
	return tempOut
            }
            
            
def convertCtoF(temperatureIn){
    log.info "Converting C to F"
    def tempIn = temperatureIn.toFloat()
    log.info "tempIn = $tempIn"
    def tempCalc = ((tempIn * 1.8) + 32)  
    def tempOut1 = tempCalc.round(state.DecimalPlaces)
    def tempOut = tempOut1
    log.info "tempOut =  $tempOut"
	return tempOut
            }   

 
def convertINtoMM(unitIn){
      log.info "Converting IN to MM"             
      def tempIn1 = unitIn.toFloat()           
      log.info "tempIn1 = $tempIn1" 
    def tempCalc1 = (tempIn1 * 25.4)
    def tempOut2 = tempCalc1.round(state.DecimalPlaces)
    def tempOut1 = tempOut2
    log.info "tempOut1 =  $tempOut1"
	return tempOut1              
               }

def convertMMtoIN(unitIn){
      log.info "Converting IN to MM"             
      def tempIn1 = unitIn.toFloat()           
      log.info "tempIn1 = $tempIn1" 
    def tempCalc1 = (tempIn1/25.4)
    def tempOut2 = tempCalc1.round(state.DecimalPlaces)
    def tempOut1 = tempOut2
    log.info "tempOut1 =  $tempOut1"
	return tempOut1              
               }               
               
               
               
def convertMBtoIN(pressureIn){
      log.info "Converting MBAR to INHg"             
     def pressIn1 = pressureIn.toFloat()           
      log.info "Pressure In = $pressIn1" 
    def pressCalc1 = (pressIn1 * 0.02953)
    def pressOut2 = pressCalc1.round(state.DecimalPlaces)
    def pressOut1 = pressOut2
    log.info "Pressure Out =  $pressOut1"
	return pressOut1              
               }                              
               
def convertINtoMB(pressureIn){
      log.info "Converting INHg to MBAR"             
      def pressIn1 = pressureIn.toFloat()           
      log.info "Pressure In = $pressIn1" 
    def pressCalc1 = (pressIn1 * 33.8638815)
    def pressOut2 = pressCalc1.round(state.DecimalPlaces)
    def pressOut1 = pressOut2
    log.info "Pressure Out =  $pressOut1"
	return pressOut1              
               }                                    
               
def convertMPHtoKPH(speed1In) {
  log.info "Converting MPH to KPH"             
      def speed1 = speed1In.toFloat()           
      log.info "Speed In = $speed1In" 
    def speedCalc1 = (speed1In * 1.60934)
    def speedOut2 = speedCalc1.round(state.DecimalPlaces)
    def speedOut1 = speedOut2
    log.info "Speed Out =  $pressOut1"
	return speedOut1              
               }                                    
               
   

def convertKPHtoMPH(speed1In) {
  log.info "Converting KPH to MPH"             
      def speed1 = speed1In.toFloat()           
      log.info "Speed In = $speed1In" 
    def speedCalc1 = (speed1In * 0.621371)
    def speedOut2 = speedCalc1.round(state.DecimalPlaces)
    def speedOut1 = speedOut2
    log.info "Speed Out =  $pressOut1"
	return speedOut1              
               }                              




















               
               
               
               
               
