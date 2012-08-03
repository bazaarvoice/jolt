Hello World from Template!

{
    "Rating" : ${rating.primary.value!""},
    "RatingRange":${rating.primary.max!""},

    "ReviewText":${text.body!""},

    "SecondaryRatings":{

        <#list rating?keys as RD>

            <#--<#if "primary" != RD.Id>-->
                "${RD}":{
                    "Id":"${RD}",
                    <#--"Value": ${rating..value}!""},-->
                    <#--"ValueRange":${RD.max}!""},-->

                    "ValueLabel":null,
                    "MaxLabel":null,
                    "Label":"Example of a wordy dimension label",
                    "MinLabel":null,
                    "DisplayType":"NORMAL"
                }
            <#--</#if>-->
            <#if RD_has_next>,</#if>
        </#list>
    }
}