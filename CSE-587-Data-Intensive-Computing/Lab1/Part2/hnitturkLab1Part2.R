install.packages("usmap")
setwd("Documents/CSE587/Lab1/Part2")
install.packages('ggmap')
install.packages('maptools')
install.packages('maps')
install.packages("devtools")
install.packages('ggplot2')

library(ggplot2)
library(usmap)
######### Part 2 Question 4 - - 1 ##########

fluview_columns1<-read.csv(file ="Data/WHO_NREVSS_Public_Health_Labs1.csv",header = T,skip = 1)
df1<-as.data.frame(fluview_columns1)
df1<-df1[,-c(0:2)]
df1<-df1[c(1,2,3,5,4,6,7,10,8,9)]
mydf1 <- data.frame(c(df1$A..H3.), c(df1$A..2009.H1N1.), c(df1$A..Subtyping.not.Performed.),c(df1$B),c(df1$H3N2v),c(df1$BVic),c(df1$BYam))
mp1<-barplot(t(as.matrix(mydf1)), col = c("red","orange","yellow","green","purple","palegreen1", "darkolivegreen3"), main="Influenza Positive Tests Reported to CDC by U.S. Public Health Laboratories,\nNational Summary, 2018-2019 Season\n\n",cex.main=0.8)
text(mp1, par('usr')[3], labels = paste(c(df1$YEAR),c(sprintf("%02d",df1$WEEK)),sep=""), srt = 45, adj = c(1.1,1.1), xpd = TRUE, cex=.9)
legend("topleft", bty="n", legend = c("A..H3.","A..2009.H1N1.","A..Subtyping.not.Performed.","B","H3N2v","BVic","BYam"), fill = c("red","orange","yellow","green","purple","palegreen1", "darkolivegreen3"))

######### Part 2 Question 4 - - 2 ##########

fluview_columns3<-read.csv(file ="Data/WHO_NREVSS_Clinical_Labs1.csv",header = T,skip = 1)
df3<-as.data.frame(fluview_columns3)
df3<-df3[,-c(0:2)]
mydf3 <- data.frame(c(df3$TOTAL.B), c(df3$TOTAL.A))
mp3<-barplot(t(as.matrix(mydf3)),ylim=c(0,18000),xlab="Week",ylab="Number of Positive Specimens", col = c("green","yellow"), main="Influenza Positive Tests Reported to CDC by U.S. \nClinical Laboratories, National Summary, 2017-2018 Season, 2018-2019 Season\n\n", cex.main=0.8)
text(mp1, par('usr')[3], labels = paste(c(df1$YEAR),c(sprintf("%02d",df1$WEEK)),sep=""), srt = 45, adj = c(1.1,1.1), xpd = TRUE, cex=.9)
par(new=TRUE)
plot(mp3,df3$PERCENT.POSITIVE,type="l",col="black",axes=FALSE,ylim=c(0,40),ann=FALSE, lty=1, yaxs = "i")
axis(4,at=seq(0,50,5))
par(new=TRUE)
plot(mp3,df3$PERCENT.A,type="l",col="yellow",axes=FALSE,ylim=c(0,40),ann=FALSE, lty=2, yaxs="i")
axis(4,at=seq(0,50,5))
par(new=TRUE)
plot(mp3,df3$PERCENT.B,type="l",col="green",axes=FALSE,ylim=c(0,40),ann=FALSE, lty=2,yaxs="i")
axis(4,at=seq(0,50,5))
legend("topleft",1, 0.12, bty="n", legend=c("A","B","Percent Positive","% Positive Flu A","% Positive Flu B"), fill=rep(c("yellow","green",NA,NA,NA),2), col=c(NA,NA,"black","yellow","green") ,pch=c(NA,NA,NA, NA, NA), lty = c(0, 0,1, 2, 2), pt.cex=1,cex=0.7)

######### Part 2 Question 4 - - 3 ##########

fluview_columns4<-read.csv(file ="Data/WHO_NREVSS_Public_Health_Labs1.csv",header = T,skip = 1)
df4<-as.data.frame(fluview_columns4)
slices<-c(sum(df4$A..Subtyping.not.Performed.), sum(df4$A..2009.H1N1.), sum(df4$A..H3.),sum(df4$B),sum(df4$H3N2v),sum(df4$BVic),sum(df4$BYam))
lbls <- c("A..H3","A..2009.H1N1","A..Subtyping.not.Performed.","B","H3N2v","BVic","BYam")
pie(slices, labels=slices,radius=1, main="Influenza Positive Specimens Reported by \nU.S. Public Health Laboratories, \nCumulative, 2018-2019 Season\n\n",col = c("yellow","orange","red","green","purple","palegreen1", "darkolivegreen3"), init.angle=90, cex.main=0.8,cex=0.7)
legend(1,1,  legend = c("A..H3.","A..2009.H1N1.","A..Subtyping.not.Performed.","B","H3N2v","BVic","BYam"), bty="n",fill = c("red","orange","yellow","green","purple","palegreen1", "darkolivegreen3"))


fluview_columns5<-read.csv(file ="Data/Genetic08.csv",header = T)
mflu<-as.data.frame(fluview_columns5)
slice1<-as.character(mflu[6,5])
slice2<-as.character(mflu[7,5])
slice3<-as.character(mflu[8,5])
label1 = paste(as.character(mflu[6,3]),as.character(mflu[6,4]),sep="\n")
label2 = paste(as.character(mflu[7,3]),as.character(mflu[7,4]),sep="\n")
label3 = paste(as.character(mflu[8,3]),as.character(mflu[8,4]),sep="\n")
result<-c(as.numeric(sub("%", "", slice1)),as.numeric(sub("%", "", slice2)),as.numeric(sub("%", "", slice3)))
alabels <- c(paste(label1,slice1,sep="\n"),paste(label2,slice2,sep="\n"),paste(label3,slice3,sep="\n"))
pie(result, labels = alabels, main="Influenza A(H3N2)",init.angle = 30, col=c("red4","red2","orangered"))


slice1<-as.character(mflu[5,5])
n<- as.character(mflu[5,3])
dc<-as.character(mflu[5,4])
pdc<-as.character(mflu[5,5])
result<-c(as.numeric(sub("%", "", slice1)))
pie(result, labels = "", main="Influenza A(H1N1)pdm09",init.angle = 90,col=c("bisque"))
#pie.labels(1,2)
text(1,-0.2,n)#pie <- ggplot(mflu, aes(x="", y=value, fill=Group))
text(1,-0.3,dc)#pie <- ggplot(mflu, aes(x="", y=value, fill=Group))
text(1,-0.4,pdc)#pie <- ggplot(mflu, aes(x="", y=value, fill=Group))


slice1<-as.character(mflu[1,5])
slice2<-as.character(mflu[2,5])
slice3<-as.character(mflu[3,5])
label1 = paste(as.character(mflu[1,3]),as.character(mflu[1,4]),sep="\n")
label2 = paste(as.character(mflu[2,3]),as.character(mflu[2,4]),sep="\n")
label3 = paste(as.character(mflu[3,3]),as.character(mflu[3,4]),sep="\n")
result<-c(as.numeric(sub("%", "", slice1)),as.numeric(sub("%", "", slice3)),as.numeric(sub("%", "", slice2)))
alabels <- c(paste(label1,slice1,sep="\n"),paste(label3,slice3,sep="\n"),paste(label2,slice2,sep="\n"))
pie(result, labels = alabels, main="Influenza B Victoria", col=c("green4","green2","lightgreen"))


slice1<-as.character(mflu[4,5])
n<- as.character(mflu[4,3])
dc<-as.character(mflu[4,4])
pdc<-as.character(mflu[4,5])
result<-c(as.numeric(sub("%", "", slice1)))
label1 = paste(as.character(mflu[4,3]),as.character(mflu[4,4]),sep="\n")
alabels <- c(paste(label1,slice1,sep="\n"))
pie(result, labels = "", main="Influenza B Yamagata",init.angle = 90,col=c("darkolivegreen3"))
text(1,-0.2,n)#pie <- ggplot(mflu, aes(x="", y=value, fill=Group))
text(1,-0.3,dc)#pie <- ggplot(mflu, aes(x="", y=value, fill=Group))
text(1,-0.4,pdc)#pie <- ggplot(mflu, aes(x="", y=value, fill=Group))


######### Part 2 Question 4 - - 4 ##########

d1<-read.csv(file ="Data/StateDataforMap_2018-19week4.csv",header = T)
d1$ACTIVITY.LEVEL<-substring(d1$ACTIVITY.LEVEL,7)
d1$ACTIVITY.LEVEL<-as.numeric(d1$ACTIVITY.LEVEL)
d1$region<-tolower(d1$STATENAME)
colnames(d1)[1]<-"state"
plot_usmap(data = d1, values = "ACTIVITY.LEVEL", lines = "black") + 
  scale_fill_gradientn(colors=c("green3","yellow1","red"),na.value="grey90" ) +
  theme(legend.position = c(0.6, -0.1))+guides(fill=guide_legend(title="ILI Activity Level",reverse=TRUE))+
  ggtitle("2018-19 Influenza Season Week 4 ending Jan 26, 2019")+
  theme(plot.title = element_text(hjust = 0.5))+ 
  annotate("text", label = "Alaska", x = 1, y = 2, hjust=4, vjust=17,size = 3.5)+
  annotate("text", label = "Hawaii", x = 1, y = 2, hjust=2.2, vjust=20, size=3.5)


######### Part 2 Question 5 - - 1 ##########

fluview_columns1<-read.csv(file ="Data/WHO_NREVSS_Public_Health_Labs.csv",header = T,skip = 1)
df1<-as.data.frame(fluview_columns1)
df1<-tail(df1,n=54)
df1<-df1[,-c(0:2)]
df1<-df1[c(1,2,3,5,4,6,7,10,8,9)]
mydf1 <- data.frame(c(df1$A..H3.), c(df1$A..2009.H1N1.), c(df1$A..Subtyping.not.Performed.),c(df1$B),c(df1$H3N2v),c(df1$BVic),c(df1$BYam))
mp1<-barplot(t(as.matrix(mydf1)), xlab="Week",ylab="Number of Positive Specimens", col = c("red","orange","yellow","green","purple","palegreen1", "darkolivegreen3"),main="Influenza Positive Tests Reported to CDC by U.S. Public Health Laboratories, \nNational Summary, 2017-2018 Season, 2018-2019 Season", cex.main=0.8)
text(mp1, par('usr')[3], labels = paste(c(df1$YEAR),c(sprintf("%02d",df1$WEEK)),sep=""), srt = 90, adj = c(1.1,1.1), xpd = TRUE, cex=0.7)
legend("topright", bty = "n",legend = c("A..H3.","A..2009.H1N1.","A..Subtyping.not.Performed.","B","H3N2v","BVic","BYam"), fill = c("red","orange","yellow","green","purple","palegreen1", "darkolivegreen3"),cex=0.7)


######### Part 2 Question 5 - - 2 ##########

fluview_columns3<-read.csv(file ="Data/WHO_NREVSS_Clinical_Labs.csv",header = T,skip = 1)
df3<-as.data.frame(fluview_columns3)
df3<-tail(df3,n=54)
df3<-df3[,-c(0:2)]
mydf3 <- data.frame(c(df3$TOTAL.B), c(df3$TOTAL.A))
mp3<-barplot(t(as.matrix(mydf3)),xlab="Week",ylab="Number of Positive Specimens", col = c("green","yellow"), main="Influenza Positive Tests Reported to CDC by U.S. \nClinical Laboratories, National Summary, 2017-2018 Season, 2018-2019 Season", cex.main=0.8)
text(mp3, par('usr')[3], labels = paste(c(df3$YEAR),c(sprintf("%02d",df1$WEEK)),sep=""), srt = 45, adj = c(1.1,1.1), xpd = TRUE, cex=.9)
par(new=TRUE)
plot(mp3,df3$PERCENT.POSITIVE,type="l",col="black",axes=FALSE,ylim=c(0,35),ann=FALSE, lty=1, yaxs = "i")
axis(4,at=seq(0,35,5))
par(new=TRUE)
plot(mp3,df3$PERCENT.A,type="l",col="red",axes=FALSE,ylim=c(0,35),ann=FALSE, lty=2, yaxs="i")
axis(4,at=seq(0,35,5))
par(new=TRUE)
plot(mp3,df3$PERCENT.B,type="l",col="green",axes=FALSE,ylim=c(0,35),ann=FALSE, lty=2,yaxs="i")
axis(4,at=seq(0,35,5))
legend("topright",1, 0.12, bty = "n", legend=c("A","B","Percent Positive","% Positive Flu A","% Positive Flu B"), fill=rep(c("yellow","green",NA,NA,NA),2), col=c(NA,NA,"black","yellow","green") ,pch=c(NA,NA,NA, NA, NA), lty = c(0, 0,1, 2, 2), pt.cex=1,cex=0.7)


######### Part 2 Question 6 - - 1 ##########

fluview_columns3<-read.csv(file ="Data/WHO_NREVSS_Clinical_Labs_BG_NY.csv",header = T,skip = 1)
df3<-as.data.frame(fluview_columns3)
df3<-tail(df3,n=54)
df3<-df3[,-c(0:2)]
mydf3 <- data.frame(c(df3$TOTAL.B), c(df3$TOTAL.A))
mp3<-barplot(t(as.matrix(mydf3)),xlab="Week",ylab="Number of Positive Specimens", col = c("green","yellow"), main="Influenza Positive Tests Reported to CDC by U.S. \nClinical Laboratories, National Summary, \n2017-2018 Season, 2018-2019 Season for New York State", cex.main=0.8)
text(mp3, par('usr')[3], labels = paste(c(df3$YEAR),c(sprintf("%02d",df1$WEEK)),sep=""), srt = 45, adj = c(1.1,1.1), xpd = TRUE, cex=.9)
par(new=TRUE)
plot(mp3,df3$PERCENT.POSITIVE,type="l",col="black",axes=FALSE,ylim=c(0,35),ann=FALSE, lty=1, yaxs = "i")
axis(4,at=seq(0,35,5))
par(new=TRUE)
plot(mp3,df3$PERCENT.A,type="l",col="red",axes=FALSE,ylim=c(0,35),ann=FALSE, lty=2, yaxs="i")
axis(4,at=seq(0,35,5))
par(new=TRUE)
plot(mp3,df3$PERCENT.B,type="l",col="green",axes=FALSE,ylim=c(0,35),ann=FALSE, lty=2,yaxs="i")
axis(4,at=seq(0,35,5))
legend("topright",1, 0.12, bty = "n", legend=c("A","B","Percent Positive","% Positive Flu A","% Positive Flu B"), fill=rep(c("yellow","green",NA,NA,NA),2), col=c(NA,NA,"black","yellow","green") ,pch=c(NA,NA,NA, NA, NA), lty = c(0, 0,1, 2, 2), pt.cex=1,cex=0.7)






















































































