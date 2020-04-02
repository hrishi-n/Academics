install.packages("twitteR")
install.packages("ggmap")
install.packages("maps")
install.packages('maptools')
install.packages("devtools")
install.packages("rtweet")
install.packages("revgeo")
install.packages("plyr")

setwd("Documents/CSE587/Lab1/Part3")
library(plyr)
library(ggmap)
library(maps)
library(maptools)

library(rtweet)
library(revgeo)
library(ggplot2)
library(usmap)

##  CDC Plot for Influenza Season
d1<-read.csv(file ="StateDataforMap_2018-19week4.csv",header = T)
d1$ACTIVITY.LEVEL<-substring(d1$ACTIVITY.LEVEL,7)
d1$ACTIVITY.LEVEL<-as.numeric(d1$ACTIVITY.LEVEL)
d1$region<-tolower(d1$STATENAME)
colnames(d1)[1]<-"state"
plot_usmap(data = d1, values = "ACTIVITY.LEVEL", lines = "black") + 
  scale_fill_gradientn(colors=c("green3","yellow1","red"),na.value="grey90",breaks = seq(min(d1$ACTIVITY.LEVEL),max(d1$ACTIVITY.LEVEL),length.out = 8)) +
  theme(legend.position = c(1.0, 0.3))+guides(fill=guide_legend(title="ILI Activity Level",reverse=TRUE))+
  ggtitle("2018-19 Influenza Season Week 4 ending Jan 26, 2019")+
  theme(plot.title = element_text(hjust = 0.5))+ 
  annotate("text", label = "Alaska", x = 1, y = 2, hjust=4, vjust=17,size = 3.5)+
  annotate("text", label = "Hawaii", x = 1, y = 2, hjust=2.2, vjust=20, size=3.5)

## Project1 Part 3
api_key<-"nIpXBB5ozZvX7JPakgYgAUABh"
api_secret<-"WKEuN9lEBeP254RIynzOjGmOUxcKKxpEshAnHmEhf082NVP1Du"

aces<-"3033164394-9KxgC02DaC3nsDnhxZc9QYSpUTEG6qPHSQ3X2oV"
aces_s<-"UXS8g1KeEBUdKWOjJWvWxhCmeYwtuPm9sIkmn30h9Qy3o"

register_google(key="AIzaSyCWSqZEvz--Wc4EdSRSY0LeUdQygtCDSLA")
create_token(
  app = "my_twitter_research_app",
  consumer_key = api_key,
  consumer_secret = api_secret,
  access_token = aces,
  access_secret = aces_s)


rt <- search_tweets(
  "#commoncold OR cold OR #cold OR #Influenza OR #cdcflu OR #fluview OR #fluvaccine OR #sniffle OR #runnynose OR flu OR Influenza OR cdcflu OR fluview OR fluvaccine OR runny nose OR sniffle OR #commoncold OR pneumonia OR #pneumonia OR viralinfection OR commoncold",
  n = 8000, include_rts = FALSE,retryonratelimit = TRUE,geocode = lookup_coords("usa"),since = "2019-01-01"
)

tweetsDF<-as.data.frame(rt)

save_as_csv(rt,"tweets.csv", prepend_ids = TRUE, na = "")

tweetsDF = read.csv("tweets.csv",row.names = NULL, stringsAsFactors=FALSE)
tweetsDF = tweetsDF[!duplicated(tweetsDF$text),]
save_as_csv(rt,"tweets_without_duplicates.csv", prepend_ids = TRUE, na = "")

library(twitteR)
setup_twitter_oauth(api_key,api_secret,aces,aces_s)
users<-lookupUsers(tweetsDF$screen_name)
usersDF<-twListToDF(users)

Encoding(usersDF$location)<-'UTF-8'
clean_location<-iconv(usersDF$location,"UTF-8","UTF-8",sub='')

userswithloc<-!is.na(clean_location)
usersloc<-clean_location[userswithloc]

locations<-geocode(usersloc)
locations<-na.omit(locations)

write.csv(locations,file = "locations.csv",row.names=FALSE, na = "")
coordinates = read.csv(file = "locations.csv", row.names = NULL, stringsAsFactors=FALSE)
coordinates$lon<-as.numeric(coordinates$lon)
coordinates$lat<-as.numeric(coordinates$lat)

coordinates <- ddply(coordinates, .(coordinates$lon, coordinates$lat), nrow)

names(coordinates) <- c("lon", "lat", "Freq")
coordinates<-as.data.frame(coordinates)
coordinates<-na.omit(coordinates)

states <- data.frame(state_name=character(1),stringsAsFactors=FALSE)

for(i in 1:nrow(coordinates)){
  print(i)
  tryCatch(
    states<-rbind(states,levels(revgeo(coordinates$lon[i],coordinates$lat[i], item='zip',provider =  'photon', output = 'frame')$state))
    ,error = function(e){
      print("NA")
      new.row <- data.frame("NA")
      states <- rbind.fill(states, new.row) 
  })
    
}


states = states[-1,]
states<-as.data.frame(states)

final_states =cbind(coordinates,states)

final_states<-as.data.frame(final_states[!(final_states$states=="State Not Found"),])
final_states<-final_states[,-c(1:2)]

final_states<-ddply(final_states, .(states), summarise, Freq=sum(Freq))
final_states$states<-tolower(final_states$states)
final_states

colnames(final_states)[1]<-"state"
plot_usmap(data = final_states, values = "Freq", lines = "black") + 
  scale_fill_gradientn(colors=c("green3","yellow1","red"),na.value="grey90", breaks = seq(min(final_states$Freq),max(final_states$Freq),length.out = 8)) +
  theme(legend.position = c(1.0, 0.5))+guides(fill=guide_legend(title="Twitter Activity Level",reverse=TRUE))+
  ggtitle("Twitter activity for flu related topics")+
  theme(plot.title = element_text(hjust = 0.5))+ 
  annotate("text", label = "Alaska", x = 1, y = 2, hjust=4, vjust=17,size = 3.5)+
  annotate("text", label = "Hawaii", x = 1, y = 2, hjust=2.2, vjust=20, size=3.5)


rt <- search_tweets(
  "#commoncold OR #Influenza",  n = 2000, include_rts = FALSE,retryonratelimit = TRUE,geocode = lookup_coords("usa"),since = "2019-01-01"
)

tweetsDF_2<-as.data.frame(rt)
save_as_csv(rt,"tweets_2.csv", prepend_ids = TRUE, na = "")
tweetsDF_2 = tweetsDF_2[!duplicated(tweetsDF_2$text),]
save_as_csv(rt,"tweets_without_duplicates_2.csv", prepend_ids = TRUE, na = "")

library(twitteR)
setup_twitter_oauth(api_key,api_secret,aces,aces_s)
users_2<-lookupUsers(tweetsDF_2$screen_name)
usersDF_2<-twListToDF(users_2)

Encoding(usersDF_2$location)<-'UTF-8'
clean_location<-iconv(usersDF_2$location,"UTF-8","UTF-8",sub='')

userswithloc<-!is.na(clean_location)
usersloc<-clean_location[userswithloc]

locations<-geocode(usersloc)
locations<-na.omit(locations)

write.csv(locations,file = "locations_2.csv",row.names=FALSE, na = "")
coordinates = read.csv(file = "locations_2.csv", row.names = NULL, stringsAsFactors=FALSE)


coordinates <- ddply(coordinates, .(coordinates$lon, coordinates$lat), nrow)

names(coordinates) <- c("lon", "lat", "Freq")
coordinates<-as.data.frame(coordinates)
coordinates<-na.omit(coordinates)

states <- data.frame(state_name=character(1),stringsAsFactors=FALSE)

for(i in 1:nrow(coordinates)){
  print(i)
  tryCatch(
    states<-rbind(states,levels(revgeo(coordinates$lon[i],coordinates$lat[i], item='zip',provider =  'photon', output = 'frame')$state))
    ,error = function(e){
      print("NA")
      new.row <- data.frame("NA")
      states <- rbind.fill(states, new.row) 
    })
  
}


states = states[-1,]
states<-as.data.frame(states)

final_states1 =cbind(coordinates,states)

final_states1<-as.data.frame(final_states1[!(final_states$states=="State Not Found"),])
final_states1<-final_states1[,-c(1:2)]

final_states1<-ddply(final_states1, .(states), summarise, Freq=sum(Freq))
final_states1$states<-tolower(final_states1$states)

colnames(final_states1)[1]<-"state"
plot_usmap(data = final_states1, values = "Freq", lines = "black") + 
  scale_fill_gradientn(colors=c("green3","yellow1","red"),na.value="grey90") +
  theme(legend.position = c(1.0, 0.5))+guides(fill=guide_legend(title="Twitter Activity Level",reverse=TRUE))+
  ggtitle("Twitter activity for flu related topics")+
  theme(plot.title = element_text(hjust = 0.5))+ 
  annotate("text", label = "Alaska", x = 1, y = 2, hjust=4, vjust=17,size = 3.5)+
  annotate("text", label = "Hawaii", x = 1, y = 2, hjust=2.2, vjust=20, size=3.5)


plot_usmap(data = final_states, values = "Freq", lines = "black")





