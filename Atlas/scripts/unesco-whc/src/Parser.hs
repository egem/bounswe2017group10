{-# LANGUAGE Arrows          #-}
{-# LANGUAGE RecordWildCards #-}

module Parser where

import qualified Data.Set          as Set
import qualified Data.Text         as T
import           Text.HandsomeSoup (css, parseHtml)
import           Text.XML.HXT.Core
import           Types

parseHeritages :: String -> IO (Set.Set Heritage)
parseHeritages body =
  Set.fromList <$> runX (parseHtml body >>> css "row" >>> parseHeritageXml)
  where
    parseHeritageXml :: ArrowXml a => a XmlTree Heritage
    parseHeritageXml = proc x -> do
      _site <- onNode "site" -< x
      _shortDesc <- onNode "short_description" -< x
      _category <- onNode "category" -< x
      _image <- onNode "image_url" -< x
      _lat <- onNode "latitude" -< x
      _long <- onNode "longitude" -< x
      _states <- T.split (== ',') ^<< onNode "states" -< x
      returnA -< Heritage {..}

    onNode :: ArrowXml a => String -> a XmlTree T.Text
    onNode name =
      css name >>> getChildren >>> isText >>> getText >>^ T.pack
