install.packages('ggmap')
install.packages('maptools')
install.packages('maps')
install.packages("devtools")
install.packages('ggplot2')

devtools::install_github("dkahle/ggmap", ref="tidyup")
#If you face issues in the above command, run the following command in the terminal: 'sudo ln -s /bin/tar /bin/gtar'

setwd("Documents/CSE587/Lab1/Part1")


#Problem 1
#assigning data to sales1
sales1<-c(12,14,16,29,30,45,19,20,16, 19, 34, 20)
#creating a poisson distribution and assigning it to sales2
sales2<-rpois(12,34)  # random numbers, Poisson distribution, mean at 34, 12 numbers
#used to set graphical parameters
par(bg="cornsilk")
#plot a graph for the sales1 data using blue color
plot(sales1, col="blue", type="o", ylim=c(0,100), xlab="Month", ylab="Sales" )
title(main="Sales by Month")
#plot dotted lines for the number obtained in the poisson distribution
lines(sales2, type="o", pch=22, lty=2, col="red")
#draw a grid - NA draws no lines, NULL draws lines along the respective axis
grid(nx=NA, ny=NULL)
#declare legend on the plot indicating Sales1 and Sales2 using colors
legend("topright", inset=.05, c("Sales1","Sales2"), fill=c("blue","red"), horiz=TRUE)



#Problem 2
#used to open dialog box to choose the file. works in R studio. does not work in jupyter notebook as jupyter is not interactive.
#sales<-read.table(file.choose(), header=T)
#used to read a text file using file path and indicate that the first row in the header
sales<-read.delim("Data/salesdata.txt", header =TRUE)
#display the data in sales
sales  # to verify that data has been read
#plot a bargraph for the data in sales with total on y-axis and values on x-axis
barplot(as.matrix(sales), main="Sales Data", ylab= "Total",beside=T, col=rainbow(5))


#Problem 3
fn1<-boxplot(sales,col=c("orange","green"))$stats

fn<-boxplot(sales,col=c("orange","green"))

fn1[3,2]
fn1[3,1]

text(1.61, fn1[3,2], paste("Median =", fn1[3,2]), adj=0, cex=0.7)
text(0.61, fn1[3,1],paste("Median =", fn1[3,1]), adj=0, cex=.7)
grid(nx=NA, ny=NA)


#Problem 4
#used to read a csv file using file path
fb1<-read.csv("Data/FB.csv")
aapl1<-read.csv("Data/AAPL.csv")

par(bg="cornsilk")
plot(aapl1$Adj.Close, col="blue", type="o", ylim=c(0,300), xlab="Days", ylab="Price" )
lines(fb1$Adj.Close, type="o", pch=22, lty=2, col="red")
legend("topright", inset=0.05, c("Apple","Facebook"), fill=c("blue","red"), horiz=TRUE)

#plot a histogram with values on x axis and  frequency on y axis
#Histograms are NOT barplots
#Histograms are used to show distributions of variables while bar charts are used to compare variables.
hist(aapl1$Adj.Close, xlab="Apple Adj Close",main="Histogram for Apple", col=rainbow(8))


#Problem 5
#data() function displays all the datasets available that come with R
#data(package="ggplot2") ->> this displays all datasets in the ggplot2 package.
data()
#library() function is used to import a particular library that can be worked upon. ggplot2 library has the mpg dataset.
library(ggplot2)
#summary function gives a summary about the dataset.
summary(mpg)
#attach() function attaches dataset to R search path. It is used to access the data in dataset directly without using the syntax "dataset_name$data"
attach(mpg)
#head() gives us first few rows of the dataset
head(mpg)
#detaches the dataset from the search path which is an important step. every dataset attached should be detached.
detach(mpg)

library(datasets)
head(sunspots,n=12)
plot(sunspots)

#Problem 6
#importing libraries
library(ggmap)
library(devtools)
library(maptools)
library(maps)
register_google(key = "Google API Key")
#plot a map of the world and mark the visited cities in that map
visited <- c("SFO", "Chennai", "London", "Melbourne", "Lima,Peru", "Johannesbury, SA")
#get geocodes on each of the cities mentioned in the visited list
ll.visited <- geocode(visited)
#set x and y parameters as the latitude and longitude values for each city
visit.x <- ll.visited$lon
visit.y <- ll.visited$lat
#display the map of the world
map("world", fill=TRUE, col="white", bg="lightblue", ylim=c(-60, 90), mar=c(0,0,0,0))
#plot points on that map for the cities. pch takes different arguments for different shapes to mark thhe cities
points(visit.x,visit.y, col="red", pch=19)

visited <- c("SFO", "New York", "Buffalo", "Dallas, TX")
ll.visited <- geocode(visited)
visit.x <- ll.visited$lon
visit.y <- ll.visited$lat
map("state", fill=TRUE, col=rainbow(50), bg="lightblue", mar=c(0,0,0,0))
points(visit.x,visit.y, col="yellow", pch=36)

#Problem 7
attach(mtcars)
head(mtcars)
#draw scatter plots for specific columns in dataset
plot(mtcars[c(1,3,4,5,6)], main="MTCARS Data")
plot(mtcars[c(1,3,4,6)], main="MTCARS Data")
plot(mtcars[c(1,3,4,6)], col=rainbow(5),main="MTCARS Data")


#Problem 8
library(ggplot2)
ggplot(mtcars, aes(x=mpg, y=disp)) + geom_point()
