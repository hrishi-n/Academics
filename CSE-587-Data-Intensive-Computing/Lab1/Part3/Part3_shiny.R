
library(shiny)

ui<-pageWithSidebar(
  headerPanel('Myfirstshiny pp'),
  sidebarPanel(
    actionButton("P3_1","CDC vs Twitter HeatMap - All Keywords"),
    actionButton("P3_2","CDC vs Twitter HeatMap - 2 or more Keywords ")
    
  ),
  mainPanel(
    textOutput("text"),
    htmlOutput("picture1"),
    htmlOutput("picture2")
  )
)

server<-function(input,output,session){
  observeEvent(input$P3_1,{
    output$text<-renderText({
      paste('We can observe that the CDC map data indicates that the infections mainly concentrated on the east coast of USA while the data from tiwtter suggests that the data the tweet concentration was more in western and souothern states such as California and Texas.')
    })
  })
  observeEvent(input$P3_1,{
    output$picture1<-renderText({
      c('<img src="',
        "CDC_HeatMap.png",
        '">'
      )
    })
  })
  observeEvent(input$P3_1,{
    
    output$picture2<-renderText({
      c('<img src="',
        "Part3_Twitter_Plot.png",
        '">'
      )
    })
  })
  observeEvent(input$P3_2,{
    
    output$picture2<-renderText({
      c('<img src="',
        "Part3_Twitter_Plot.png",
        '">'
      )
    })
  })
}

shinyApp(ui,server)
