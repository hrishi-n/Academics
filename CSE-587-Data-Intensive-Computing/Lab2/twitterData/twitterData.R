setwd("Documents/CSE587/Lab2/twitterData")

api_key<-"ivm4AFkCIDR4RvC2nmZOz41NX"
api_secret<-"VpvETdBtZMfhjXq5zxIMyAcqK8fb6zdyH1WxpdFX94MBljtr1C"

aces<-"3033164394-WcB74lWRfEmYjriKcYwxxEqBFk9bmejHGlI8Kt6"
aces_s<-"wZiP0eAn1AqqgHE5LJPq5PzbpqaQMbsELRUsadWyxhrqz"

install.packages("rtweet")

library(rtweet)

create_token(
  app = "my_twitter_research_app",
  consumer_key = api_key,
  consumer_secret = api_secret,
  access_token = aces,
  access_secret = aces_s)

topicList=c("golf","baseball")


rt <- search_tweets("dog", n = 100, include_rts = FALSE,geocode = lookup_coords("usa"), since = "2019-01-01")


j=0
for (i in topicList){
  print(i)
  if(j==4){
    Sys.sleep(900)
  }
rt <- search_tweets(
  i,n = 4000, include_rts = FALSE,geocode = lookup_coords("usa"),since = "2019-01-01"
)

rt = rt[!duplicated(rt$text),]

tweets<-data.frame(rt$text)
#dir.create(i)
#dir.create("alldata")
save_as_csv(rt, paste(i,paste(i,"raw_tweets.csv",sep = "_"),sep = "/"), prepend_ids = TRUE, na = "", fileEncoding = "UTF-8")
save_as_csv(tweets, paste(i,paste(i,"tweets_text.csv",sep = "_"),sep = "/"), prepend_ids = TRUE, na = "", fileEncoding = "UTF-8")
save_as_csv(tweets, paste("alldata",paste(i,"tweets_text.csv",sep = "_"),sep = "/"), prepend_ids = TRUE, na = "", fileEncoding = "UTF-8")
j=j+1
}






